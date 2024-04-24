package com.lichenaut.cancelsprint.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CSTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] strings) {
        List<String> options = new ArrayList<>();
        if (strings.length == 1 && commandSender instanceof Player player) {
            if (player.hasPermission("cancelsprint.help")) options.add("help");
            options.add("mute");
            if (player.hasPermission("cancelsprint.reload")) options.add("reload");
        }
        return options;
    }
}
