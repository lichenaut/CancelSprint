package com.lichenaut.cancelsprint;

import com.lichenaut.cancelsprint.cmd.CSCommand;
import com.lichenaut.cancelsprint.cmd.CSTabCompleter;
import com.lichenaut.cancelsprint.db.CSSQLiteManager;
import com.lichenaut.cancelsprint.event.CSSprint;
import com.lichenaut.cancelsprint.runnable.CSRunnableManager;
import com.lichenaut.cancelsprint.runnable.CSTeleporter;
import com.lichenaut.cancelsprint.util.CSCopier;
import com.lichenaut.cancelsprint.util.CSMessager;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
@SuppressWarnings("unused")
public final class Main extends JavaPlugin {

    public static final String separator = FileSystems.getDefault().getSeparator();
    public static final HashSet<UUID> muters = new HashSet<>();
    public static final Map<UUID, Location> sprintStarts = new HashMap<>();
    private final PluginManager pluginManager = getServer().getPluginManager();
    private final Logger logging = LogManager.getLogger("CancelSprint");
    private final CSMessager messager = new CSMessager(this);
    private final CSSQLiteManager databaseManager = new CSSQLiteManager();
    private final CSRunnableManager teleportManager = new CSRunnableManager(this);
    private Configuration configuration;
    private CompletableFuture<Void> mainFuture = CompletableFuture.completedFuture(null);
    private PluginCommand csCommand;
    private Instant checkStart;
    private boolean instantDisable = false;
    private long checkInterval;

    @Override
    public void onEnable() {
        new MetricsLite(this, 21695);

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        reloadCS();

        if (instantDisable) return;

        csCommand = Objects.requireNonNull(getCommand("cs"));
        mainFuture = mainFuture
                .thenAcceptAsync(queued -> {
                    csCommand.setExecutor(new CSCommand(this, messager));
                    csCommand.setTabCompleter(new CSTabCompleter());
                })
                .exceptionallyAsync(e -> {
                    logging.error("Error while setting up plugin command!");
                    disablePlugin(e);
                    return null;
                });

        mainFuture = mainFuture
                .thenAcceptAsync(commandsSet -> {
                    try {
                        databaseManager.deserializeMuters();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionallyAsync(e -> {
                    logging.error("Error while deserializing message muter list!");
                    disablePlugin(e);
                    return null;
                });
    }

    public void reloadCS() {
        reloadConfig();
        configuration = getConfig();

        HandlerList.unregisterAll(this);

        if (configuration.getBoolean("disable-plugin")) {
            logging.info("Plugin disabled in config.yml.");
            instantDisable = true;
            disablePlugin();
        }

        if (instantDisable) return;

        mainFuture = mainFuture
                .thenAcceptAsync(disabledChecked -> {
                    String localesFolderString = getDataFolder().getPath() + separator + "locales";
                    try {
                        Path localesFolderPath = Path.of(localesFolderString);
                        if (!Files.exists(localesFolderPath)) Files.createDirectory(localesFolderPath);
                        String[] localeFiles = {separator + "de.properties", separator + "en.properties", separator + "es.properties", separator + "fr.properties"};
                        for (String locale : localeFiles) CSCopier.smallCopy(getResource("locales" + locale), localesFolderString + locale);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        messager.loadLocaleMessages(localesFolderString);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionallyAsync(e -> {
                    logging.error("Error while creating locale files and loading locale messages!");
                    disablePlugin(e);
                    return null;
                });

        mainFuture = mainFuture
                .thenAcceptAsync(localesLoaded -> {
                    try {
                        CSCopier.smallCopy(getResource("muters.db"), getDataFolder().getPath() + separator + "muters.db");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionallyAsync(e -> {
                    logging.error("Error while creating local database file!");
                    disablePlugin(e);
                    return null;
                });

        mainFuture = mainFuture
                .thenAcceptAsync(dbCopied -> databaseManager.initializeDataSource())
                .exceptionallyAsync(e -> {
                    logging.error("Error while setting up database!");
                    disablePlugin(e);
                    return null;
                });

        mainFuture = mainFuture
                .thenAcceptAsync(connected -> {
                    try {
                        databaseManager.createStructure();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionallyAsync(e -> {
                    logging.error("Error while creating database structure!");
                    disablePlugin(e);
                    return null;
                });


        mainFuture = mainFuture
                .thenAcceptAsync(structured -> pluginManager.registerEvents(new CSSprint(), this))
                .exceptionallyAsync(e -> {
                    logging.error("Error while registering sprint event!");
                    disablePlugin(e);
                    return null;
                });

        checkInterval = configuration.getLong("ticks-per-check");
        mainFuture = mainFuture
                .thenAcceptAsync(registered -> {
                    teleportManager.addRunnable(new CSTeleporter(this), checkInterval);
                    checkStart = Instant.now();
                })
                .exceptionallyAsync(e -> {
                    logging.error("Error while queuing sprint checker!");
                    disablePlugin(e);
                    return null;
                });
    }

    @Override
    public void onDisable() {
        if (instantDisable) return;

        mainFuture = mainFuture
                .thenAcceptAsync(disabled -> {
                    try {
                        databaseManager.serializeMuters();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } finally {
                        databaseManager.closeDataSource();
                    }
                })
                .exceptionallyAsync(e -> {
                    logging.error("Error while serializing message muter list!");
                    logging.error(e);
                    return null;
                });
    }

    private void disablePlugin() { pluginManager.disablePlugin(this); }

    private void disablePlugin(Object e) {
        logging.error(e);
        pluginManager.disablePlugin(this);
    }
}
