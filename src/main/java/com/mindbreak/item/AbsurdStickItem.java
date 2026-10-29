package com.mindbreak.item;

import com.mindbreak.chaos.ChaosManager;
import com.mindbreak.chaos.ChaosUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 离谱棒：右键随机整一个活。用完不消耗，因为后悔没有终点。
 */
public class AbsurdStickItem extends Item {
    public AbsurdStickItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            ChaosManager.fireRandom(serverPlayer.getServer(), serverPlayer);
            ChaosUtil.sound(serverPlayer, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.8F, 1.8F);
        }
        return TypedActionResult.success(stack);
    }
}