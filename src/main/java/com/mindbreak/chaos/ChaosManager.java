package com.mindbreak.chaos;

import java.util.List;
import java.util.Random;

import com.mindbreak.MindbreakMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 混沌引擎：负责按强度随机触发整活事件、发环境消息、跑延迟任务。
 */
public final class ChaosManager {
    public static final Random RANDOM = new Random();

    private static boolean enabled = true;
    private static int intensity = 2;
    private static long eventsFired = 0L;
    private static int depth = 0;

    private ChaosManager() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static int getIntensity() {
        return intensity;
    }

    public static void setIntensity(int value) {
        intensity = Math.max(1, Math.min(3, value));
    }

    public static long getEventsFired() {
        return eventsFired;
    }

    public static void tick(MinecraftServer server) {
        ChaosScheduler.tick(server);
        if (!enabled) {
            return;
        }
        if (server.getTicks() % 20 != 0) {
            return;
        }
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            if (player.isSpectator() || player.isRemoved()) {
                continue;
            }
            // 每秒 7%/10%/13% 概率出事（强度 1/2/3）
            if (RANDOM.nextInt(100) < 4 + intensity * 3) {
                fireRandom(server, player);
            } else if (RANDOM.nextInt(70) == 0) {
                player.sendMessage(net.minecraft.text.Text.literal(Msgs.frame(Msgs.pick(RANDOM, Msgs.AMBIENT))), false);
            }
        }
    }

    public static boolean fireRandom(MinecraftServer server, ServerPlayerEntity player) {
        if (depth > 1) {
            return false;
        }
        TrollEvent event = ChaosEvents.pick(RANDOM);
        return fire(event, server, player);
    }

    public static boolean fireById(String id, MinecraftServer server, ServerPlayerEntity player) {
        TrollEvent event = ChaosEvents.byId(id);
        if (event == null) {
            return false;
        }
        return fire(event, server, player);
    }

    private static boolean fire(TrollEvent event, MinecraftServer server, ServerPlayerEntity player) {
        depth++;
        try {
            boolean happened = event.trigger(server, player);
            if (happened) {
                eventsFired++;
            }
            return happened;
        } catch (Exception exception) {
            MindbreakMod.LOGGER.warn("[精神污染] 整活事件 {} 翻车了，这很合理：{}", event.id(), exception.toString());
            return false;
        } finally {
            depth--;
        }
    }
}