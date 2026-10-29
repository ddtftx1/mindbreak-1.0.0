package com.mindbreak.item;

import com.mindbreak.chaos.ChaosUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 后悔蛋糕：吃完会后悔，但吃之前你不会相信。
 */
public class RegretCakeItem extends Item {
    public RegretCakeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0));
            ChaosUtil.msg(serverPlayer, "你尝到了后悔的味道。");
        }
        return super.use(world, user, hand);
    }
}