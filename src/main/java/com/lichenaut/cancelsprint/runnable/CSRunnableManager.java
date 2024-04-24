package com.lichenaut.cancelsprint.runnable;

import com.lichenaut.cancelsprint.Main;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;

@Getter
public class CSRunnableManager {

    private final Main main;
    private final BukkitScheduler scheduler;
    private final LinkedList<CSRunnable> runnableQueue = new LinkedList<>();
    private BukkitTask currentTask;

    public CSRunnableManager(Main main) {
        this.main = main;
        scheduler = main.getServer().getScheduler();
    }

    public void addRunnable(BukkitRunnable bukkitRunnable, long delay) {
        runnableQueue.offer(new CSRunnable(main, bukkitRunnable, delay));
        if (currentTask == null) scheduleNextRunnable();
    }

    private void scheduleNextRunnable() {
        CSRunnable runnable = runnableQueue.poll();
        if (runnable == null) return;

        currentTask = scheduler.runTaskLater(main, () -> {
            try {
                runnable.run();
            } finally {
                currentTask = null;
                scheduleNextRunnable();
            }
        }, runnable.delay());
    }
}