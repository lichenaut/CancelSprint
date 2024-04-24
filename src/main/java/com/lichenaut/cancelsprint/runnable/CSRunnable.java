package com.lichenaut.cancelsprint.runnable;

import com.lichenaut.cancelsprint.Main;
import org.bukkit.scheduler.BukkitRunnable;

public record CSRunnable(Main main, BukkitRunnable runnable, long delay) {

    public void run() { runnable.run(); }
}