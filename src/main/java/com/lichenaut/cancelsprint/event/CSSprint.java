package com.lichenaut.cancelsprint.event;

import com.lichenaut.cancelsprint.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class CSSprint implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerSprint(PlayerToggleSprintEvent event) {
        if (!event.isSprinting()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("cancelsprint.bypass")) return;

        Main.sprintStarts.put(player.getUniqueId(), player.getLocation());
    }
}
