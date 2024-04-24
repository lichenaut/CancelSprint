package com.lichenaut.cancelsprint.util;

import com.lichenaut.cancelsprint.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@RequiredArgsConstructor
public class CSMessager {

    private final Main main;
    private final Properties properties = new Properties();
    private String locale;
    private BaseComponent[] prefix;
    private BaseComponent[] helpCommand;
    private BaseComponent[] invalidCommand;
    private BaseComponent[] reloadCommand;
    private BaseComponent[] onlyPlayerCommand;
    private BaseComponent[] sprintingDisabled;
    private BaseComponent[] muteCommand1;
    private BaseComponent[] muteCommand2;

    public void loadLocaleMessages(String localesFolderString) throws IOException {
        properties.clear();
        locale = main.getConfiguration().getString("locale");
        try (FileInputStream inputStream = new FileInputStream(new File(localesFolderString, locale + ".properties"))) {
            properties.load(inputStream);
            prefix = loadMessage("prefix");
            helpCommand = loadMessage("helpCommand");
            invalidCommand = loadMessage("invalidCommand");
            reloadCommand = loadMessage("reloadCommand");
            onlyPlayerCommand = loadMessage("onlyPlayerCommand");
            sprintingDisabled = loadMessage("sprintingDisabled");
            muteCommand1 = loadMessage("muteCommand1");
            muteCommand2 = loadMessage("muteCommand2");
        }
    }

    private BaseComponent[] loadMessage(String key) {
        String message = properties.getProperty(key);
        if (message == null) {
            main.getLogging().error("Missing message key: \"{}\" in locale: \"{}.properties\".", key, locale);
            return new BaseComponent[]{new TextComponent("")};
        }

        Pattern pattern = Pattern.compile("<([^>]+)>(.*?)\\s*(?=<[^>]+>|\\z)");
        Matcher matcher = pattern.matcher(message);
        ArrayList<String> resultList = new ArrayList<>();
        while (matcher.find()) {
            resultList.add(matcher.group(2));
            resultList.add(matcher.group(1));
        }

        ComponentBuilder builder = new ComponentBuilder("");
        for (String part : resultList) {
            switch (part.toLowerCase()) {
                case "aqua":
                    builder.color(ChatColor.AQUA);
                    break;
                case "black":
                    builder.color(ChatColor.BLACK);
                    break;
                case "blue":
                    builder.color(ChatColor.BLUE);
                    break;
                case "bold":
                    builder.bold(true);
                    break;
                case "dark_aqua":
                    builder.color(ChatColor.DARK_AQUA);
                    break;
                case "dark_blue":
                    builder.color(ChatColor.DARK_BLUE);
                    break;
                case "dark_gray":
                    builder.color(ChatColor.DARK_GRAY);
                    break;
                case "dark_green":
                    builder.color(ChatColor.DARK_GREEN);
                    break;
                case "dark_purple":
                    builder.color(ChatColor.DARK_PURPLE);
                    break;
                case "dark_red":
                    builder.color(ChatColor.DARK_RED);
                    break;
                case "gold":
                    builder.color(ChatColor.GOLD);
                    break;
                case "gray":
                    builder.color(ChatColor.GRAY);
                    break;
                case "green":
                    builder.color(ChatColor.GREEN);
                    break;
                case "italic":
                    builder.italic(true);
                    break;
                case "light_purple":
                    builder.color(ChatColor.LIGHT_PURPLE);
                    break;
                case "magic":
                    builder.obfuscated(true);
                    break;
                case "red":
                    builder.color(ChatColor.RED);
                    break;
                case "reset":
                    builder.reset();
                    break;
                case "strikethrough":
                    builder.strikethrough(true);
                    break;
                case "underline":
                    builder.underlined(true);
                    break;
                case "white":
                    builder.color(ChatColor.WHITE);
                    break;
                case "yellow":
                    builder.color(ChatColor.YELLOW);
                    break;
                default:
                    builder.append(part);
                    break;
            }
        }
        builder.append(" ");
        return builder.create();
    }

    public void sendMsg(CommandSender sender, BaseComponent[] message, boolean includePrefix) {
        if (sender instanceof Player) {
            if (prefix == null || !includePrefix) {
                sender.spigot().sendMessage(message);
            } else {
                sender.spigot().sendMessage(concatArrays(prefix, message));
            }
            return;
        }

        infoLog(message);
    }

    public BaseComponent[] concatArrays(BaseComponent[]... arrays) {
        ArrayList<BaseComponent> resultList = new ArrayList<>();
        for (BaseComponent[] array : arrays) resultList.addAll(Arrays.asList(array));
        return resultList.toArray(new BaseComponent[0]);
    }

    private void infoLog(BaseComponent[] message) {
        main.getLogging().info(new TextComponent(message).toLegacyText().replaceAll("§[0-9a-fA-FklmnoKLMNO]", ""));
    }
}