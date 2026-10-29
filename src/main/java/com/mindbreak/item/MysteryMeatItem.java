package com.mindbreak.item;

import com.mindbreak.chaos.ChaosManager;
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
 * 神秘不明肉：一半概率是好东西，一半概率你会记住它一辈子。
 */
public class MysteryMeatItem extends Item {
    public MysteryMeatItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            if (ChaosManager.RANDOM.nextBoolean()) {
                serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0));
                serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 40, 0));
                ChaosUtil.msg(serverPlayer, "这块肉居然还挺好吃……你有点感动。");
            } else {
                serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 160, 1));
                serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 2));
                ChaosUtil.msg(serverPlayer, "你后悔了。你现在非常后悔。");
            }
        }
        return super.use(world, user, hand);
    }
}