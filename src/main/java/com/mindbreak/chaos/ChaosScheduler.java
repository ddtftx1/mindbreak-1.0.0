package com.mindbreak.chaos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mindbreak.MindbreakMod;
import net.minecraft.server.MinecraftServer;

/**
 * 一个极简的延迟任务调度器，用来做"偷你的东西 10 秒后还给你"这种阴间操作。
 */
public final class ChaosScheduler {
    private static final List<Scheduled> TASKS = new ArrayList<>();

    private ChaosScheduler() {
    }

    private static final class Scheduled {
        final int due;
        final Runnable action;

        Scheduled(int due, Runnable action) {
            this.due = due;
            this.action = action;
        }
    }

    public static void schedule(MinecraftServer server, int delayTicks, Runnable action) {
        TASKS.add(new Scheduled(server.getTicks() + Math.max(1, delayTicks), action));
    }

    public static void tick(MinecraftServer server) {
        if (TASKS.isEmpty()) {
            return;
        }
        List<Scheduled> due = new ArrayList<>();
        Iterator<Scheduled> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            Scheduled task = iterator.next();
            if (server.getTicks() >= task.due) {
                iterator.remove();
                due.add(task);
            }
        }
        for (Scheduled task : due) {
            try {
                task.action.run();
            } catch (Exception exception) {
                MindbreakMod.LOGGER.warn("[精神污染] 延迟任务翻车了，没关系，下次还敢。", exception);
            }
        }
    }
}