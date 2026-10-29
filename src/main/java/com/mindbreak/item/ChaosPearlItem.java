package com.mindbreak.item;

import com.mindbreak.chaos.ChaosUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

/**
 * 混乱珍珠：随机把你扔到 100 格开外的某个地方。方向感不提供。
 */
public class ChaosPearlItem extends Item {
    public ChaosPearlItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            ServerWorld serverWorld = serverPlayer.getServerWorld();
            double startX = serverPlayer.getX();
            double startZ = serverPlayer.getZ();
            double x = startX + ChaosUtil.offset(96.0);
            double z = startZ + ChaosUtil.offset(96.0);
            int y = serverWorld.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            ChaosUtil.teleport(serverPlayer, x, y + 1, z);
            ChaosUtil.sound(serverPlayer, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.2F);
            int distance = (int) Math.sqrt((x - startX) * (x - startX) + (z - startZ) * (z - startZ));
            ChaosUtil.msg(serverPlayer, "你被扔到了大约 " + distance + " 格开外。方向感？不需要。");
            stack.decrement(1);
        }
        return TypedActionResult.success(stack);
    }
}