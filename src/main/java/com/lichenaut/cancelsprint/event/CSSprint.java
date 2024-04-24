package com.lichenaut.cancelsprint.event;

import com.lichenaut.cancelsprint.Main;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CSSprint implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerSprint(PlayerToggleSprintEvent event) {
        if (!event.isSprinting()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("cancelsprint.bypass")) return;

        Main.sprintStarts.put(player.getUniqueId(), player.getLocation());
    }
}
