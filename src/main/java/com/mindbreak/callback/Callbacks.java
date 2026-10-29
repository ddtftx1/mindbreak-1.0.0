package com.mindbreak.callback;

import java.util.Random;

import com.mindbreak.chaos.ChaosManager;
import com.mindbreak.chaos.ChaosUtil;
import com.mindbreak.chaos.Msgs;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * 所有"你干点什么就会被搞"的回调。
 */
public final class Callbacks {
    private static final Random RANDOM = ChaosManager.RANDOM;

    private Callbacks() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ChaosManager::tick);
        PlayerBlockBreakEvents.BEFORE.register(Callbacks::onBeforeBreak);
        PlayerBlockBreakEvents.AFTER.register(Callbacks::onAfterBreak);
        UseBlockCallback.EVENT.register(Callbacks::onUseBlock);
        AttackEntityCallback.EVENT.register(Callbacks::onAttackEntity);
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(Callbacks::onChatMessage);
        ServerLivingEntityEvents.AFTER_DEATH.register(Callbacks::onDeath);
        ServerPlayerEvents.JOIN.register(Callbacks::onJoin);
        ServerPlayerEvents.AFTER_RESPAWN.register(Callbacks::onRespawn);
    }

    /**
     * 挖方块有概率被方块拒绝。
     */
    private static boolean onBeforeBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
                                         @Nullable BlockEntity blockEntity) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer) || !ChaosManager.isEnabled()) {
            return true;
        }
        if (player.isCreative()) {
            return true;
        }
        if (RANDOM.nextInt(100) < 5) {
            ChaosUtil.sound(serverPlayer, SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 0.8F);
            ChaosUtil.msg(serverPlayer, "这个" + state.getBlock().getName().getString() + "太紧张了，趁你不注意逃走了。");
            return false;
        }
        return true;
    }

    /**
     * 挖掉的方块有概率"长"成一个奇怪的东西。
     */
    private static void onAfterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
                                     @Nullable BlockEntity blockEntity) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity) || !ChaosManager.isEnabled()) {
            return;
        }
        if (RANDOM.nextInt(100) < 4) {
            Block[] weird = {Blocks.PUMPKIN, Blocks.MELON, Blocks.CAKE, Blocks.JUKEBOX, Blocks.SPONGE, Blocks.SLIME_BLOCK};
            Block block = weird[RANDOM.nextInt(weird.length)];
            world.setBlockState(pos, block.getDefaultState());
            ChaosUtil.msg((ServerPlayerEntity) player,
                    "你挖掉的方块又长回来了，但它现在是一个" + block.getName().getString() + "。");
        }
    }

    /**
     * 右键：床拒绝你，方块半路变身。
     */
    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer) || !ChaosManager.isEnabled()) {
            return ActionResult.PASS;
        }
        if (hand == Hand.MAIN_HAND && world.getBlockState(hitResult.getBlockPos()).isIn(net.minecraft.registry.tag.BlockTags.BEDS)
                && RANDOM.nextInt(100) < 15) {
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 50, 1));
            ChaosUtil.sound(serverPlayer, SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 0.7F);
            ChaosUtil.msg(serverPlayer, "床：不，你不想睡。");
            return ActionResult.FAIL;
        }

        ItemStack held = player.getStackInHand(hand);
        if (held.getItem() instanceof BlockItem && RANDOM.nextInt(100) < 8) {
            Item replacement = pickReplacementBlock();
            player.setStackInHand(hand, new ItemStack(replacement));
            ChaosUtil.sound(serverPlayer, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 0.5F);
            ChaosUtil.msg(serverPlayer,
                    "你的方块在最后一刻变成了" + replacement.getName().getString() + "，它说它更喜欢这样。");
        }
        return ActionResult.PASS;
    }

    private static Item pickReplacementBlock() {
        Item[] pool = {Items.MELON, Items.CAKE, Items.PUMPKIN, Items.SLIME_BLOCK, Items.SPONGE,
                Items.JUKEBOX, Items.DIAMOND_BLOCK, Items.TNT };
        return pool[RANDOM.nextInt(pool.length)];
    }

    /**
     * 打怪有概率被自己的武器背刺，怪物有概率嘴臭。
     */
    private static ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity,
                                              @Nullable EntityHitResult hitResult) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer) || !ChaosManager.isEnabled()) {
            return ActionResult.PASS;
        }
        if (RANDOM.nextInt(100) < 4) {
            serverPlayer.damage(world.getDamageSources().magic(), 2.0F);
            ChaosUtil.sound(serverPlayer, SoundEvents.ENTITY_PLAYER_HURT, 1.0F, 1.0F);
            ChaosUtil.msg(serverPlayer, "你的武器突然对你产生了兴趣。");
        } else if (RANDOM.nextInt(100) < 7) {
            ChaosUtil.broadcast(serverPlayer, "【" + entity.getName().getString() + "】" + Msgs.pick(RANDOM, Msgs.TAUNTS));
        } else if (entity instanceof LivingEntity living && RANDOM.nextInt(100) < 5) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 2));
            ChaosUtil.msg(serverPlayer, "你把它打兴奋了。它现在的速度是 3。");
        }
        return ActionResult.PASS;
    }

    /**
     * 你说的话有 18% 概率被曲解，然后用"你的名字"发出去。
     */
    private static boolean onChatMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        if (!ChaosManager.isEnabled() || RANDOM.nextInt(100) >= 18) {
            return true;
        }
        String original = message.getContent().getString();
        String mangled = Msgs.mangle(original);
        sender.getServer().getPlayerManager().broadcast(
                Text.literal("<" + sender.getName().getString() + "> " + mangled), false);
        return false;
    }

    /**
     * 死了之后有概率留下遗言（全村通报）。
     */
    private static void onDeath(LivingEntity entity, net.minecraft.entity.damage.DamageSource source) {
        if (entity instanceof ServerPlayerEntity serverPlayer && ChaosManager.isEnabled() && RANDOM.nextInt(100) < 60) {
            ChaosUtil.broadcast(serverPlayer, "【遗言】" + serverPlayer.getName().getString() + " "
                    + Msgs.pick(RANDOM, Msgs.DEATH));
        }
    }

    private static void onJoin(ServerPlayerEntity player) {
        if (RANDOM.nextInt(100) < 70) {
            player.sendMessage(Text.literal("【精神污染】欢迎回来，" + player.getName().getString()
                    + "。本模组承诺：不会让你无聊，只会让你崩溃。"), false);
        }
    }

    private static void onRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        newPlayer.sendMessage(Text.literal("【精神污染】你回来了？它们记得你。"), false);
    }
}