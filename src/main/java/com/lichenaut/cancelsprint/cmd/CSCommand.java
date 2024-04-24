package com.lichenaut.cancelsprint.cmd;

import com.lichenaut.cancelsprint.Main;
import com.lichenaut.cancelsprint.util.CSMessager;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.processing.Messager;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CSCommand implements CommandExecutor {

    private static CompletableFuture<Void> commandFuture = CompletableFuture.completedFuture(null);
    private final Main main;
    private final CSMessager messager;

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] strings) {
        if (strings.length == 0) {
            commandFuture = commandFuture
                    .thenAcceptAsync(processed -> messager.sendMsg(commandSender, messager.getInvalidCommand(), false));
            return true;
        }

        switch (strings[0]) {
            case "help" -> {
                if (checkDisallowed(commandSender, "cancelsprint.help")) return true;

                commandFuture = commandFuture
                        .thenAcceptAsync(processed -> messager.sendMsg(commandSender, messager.getHelpCommand(), false));
                return true;
            }
            case "mute" -> {
                if (!(commandSender instanceof Player player)) {
                    commandFuture = commandFuture
                            .thenAcceptAsync(processed -> messager.sendMsg(commandSender, messager.getOnlyPlayerCommand(), false));
                } else {
                    if (Main.muters.contains(player.getUniqueId())) {
                        Main.muters.remove(player.getUniqueId());

                        commandFuture = commandFuture
                                .thenAcceptAsync(processed -> messager.sendMsg(commandSender, messager.getMuteCommand2(), true));
                    } else {
                        Main.muters.add(player.getUniqueId());

                        commandFuture = commandFuture
                                .thenAcceptAsync(processed -> messager.sendMsg(commandSender, messager.getMuteCommand1(), true));
                    }
                }
                return true;
            }
            case "reload" -> {
                if (checkDisallowed(commandSender, "cancelsprint.reload")) return true;

                main.reloadCS();

                commandFuture = commandFuture
                        .thenAcceptAsync(processed -> messager.sendMsg(commandSender, messager.getReloadCommand(), false));
                return true;
            }
        }

        return true;
    }

    private boolean checkDisallowed(CommandSender sender, String permission) {
        return sender instanceof Player && !sender.hasPermission(permission);
    }
}
