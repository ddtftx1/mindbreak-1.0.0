package com.mindbreak.chaos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 一个"整活事件"：有 id、权重，以及触发逻辑。
 * 触发逻辑返回 true 表示这次真的搞到人了（用于统计和命令反馈）。
 */
public final class TrollEvent {
    private final String id;
    private final int weight;
    private final BiPredicate<MinecraftServer, ServerPlayerEntity> action;

    public TrollEvent(String id, int weight, BiPredicate<MinecraftServer, ServerPlayerEntity> action) {
        this.id = id;
        this.weight = weight;
        this.action = action;
    }

    public String id() {
        return id;
    }

    public int weight() {
        return weight;
    }

    public boolean trigger(MinecraftServer server, ServerPlayerEntity player) {
        return action.test(server, player);
    }

    public static List<String> ids(List<TrollEvent> events) {
        List<String> ids = new ArrayList<>(events.size());
        for (TrollEvent event : events) {
            ids.add(event.id());
        }
        return ids;
    }
}