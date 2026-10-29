package com.mindbreak.item;

import com.mindbreak.chaos.ChaosManager;
import com.mindbreak.chaos.ChaosUtil;
import com.mindbreak.chaos.Msgs;
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
 * 嘴臭之书：对全世界输出一句不带脏字但很伤人的话。
 */
public class CurseBookItem extends Item {
    public CurseBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayerEntity target = null;
            for (ServerPlayerEntity other : serverPlayer.getServer().getPlayerManager().getPlayerList()) {
                if (other != serverPlayer) {
                    target = other;
                }
            }
            String targetName = target == null ? "全世界" : target.getName().getString();
            ChaosUtil.broadcast(serverPlayer, serverPlayer.getName().getString() + " 对 " + targetName + " 说："
                    + Msgs.pick(ChaosManager.RANDOM, Msgs.CURSE));
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
        }
        return TypedActionResult.success(stack);
    }
}