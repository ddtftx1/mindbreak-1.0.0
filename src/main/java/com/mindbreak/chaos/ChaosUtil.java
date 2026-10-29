package com.mindbreak.chaos;

import java.util.List;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 小工具集合，专门用来祸害玩家。
 */
public final class ChaosUtil {
    private ChaosUtil() {
    }

    public static ServerWorld world(ServerPlayerEntity player) {
        return player.getServerWorld();
    }

    public static void msg(ServerPlayerEntity player, String text) {
        player.sendMessage(Text.literal(Msgs.frame(text)), false);
    }

    public static void broadcast(ServerPlayerEntity player, String text) {
        player.getServer().getPlayerManager().broadcast(Text.literal(Msgs.frame(text)), false);
    }

    public static void sound(ServerPlayerEntity player, SoundEvent sound, float volume, float pitch) {
        ServerWorld world = world(player);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundCategory.PLAYERS, volume, pitch);
    }

    public static void particle(ServerPlayerEntity player, ParticleEffect particle, int count) {
        ServerWorld world = world(player);
        world.spawnParticles(particle, player.getX(), player.getY() + 1.0, player.getZ(), count, 0.7, 0.9, 0.7, 0.05);
    }

    public static void teleport(ServerPlayerEntity player, double x, double y, double z) {
        player.teleport(world(player), x, y, z, player.getYaw(), player.getPitch());
    }

    public static void teleportRot(ServerPlayerEntity player, double x, double y, double z, float yaw, float pitch) {
        player.teleport(world(player), x, y, z, yaw, pitch);
    }

    public static void give(ServerPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    public static ItemStack named(Item item, String name, Formatting formatting) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(formatting));
        return stack;
    }

    public static int rnd(int bound) {
        return ChaosManager.RANDOM.nextInt(bound);
    }

    public static boolean chance(int percent) {
        return ChaosManager.RANDOM.nextInt(100) < percent;
    }

    public static double offset(double range) {
        return (ChaosManager.RANDOM.nextDouble() * 2.0 - 1.0) * range;
    }
}