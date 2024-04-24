package com.lichenaut.cancelsprint.runnable;

import com.lichenaut.cancelsprint.Main;
import com.lichenaut.cancelsprint.util.CSMessager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class CSTeleporter extends BukkitRunnable {

    private final Main main;
    private final Server server;

    public CSTeleporter(Main main) {
        this.main = main;
        this.server = main.getServer();
    }

    @Override
    public void run() {
        main.getTeleportManager().addRunnable(this, main.getCheckInterval());
        for (UUID uuid : Main.sprintStarts.keySet()) {
            Player player = server.getPlayer(uuid);
            if (player == null) return;

            Location location = Main.sprintStarts.get(uuid);
            if (location == null) return;

            if (!player.isSprinting()) Main.sprintStarts.remove(player.getUniqueId());
            player.teleport(location);
            CSMessager messager = main.getMessager();
            if (!Main.muters.contains(uuid)) messager.sendMsg(player, messager.getSprintingDisabled(), true);
        }
    }
}
