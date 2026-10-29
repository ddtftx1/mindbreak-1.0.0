package com.mindbreak.chaos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

/**
 * 所有"整活事件"的注册表。每个事件返回 true 表示确实搞到人了。
 */
public final class ChaosEvents {
    private static final List<TrollEvent> EVENTS = new ArrayList<>();

    private ChaosEvents() {
    }

    private static void add(String id, int weight, java.util.function.BiPredicate<MinecraftServer, ServerPlayerEntity> action) {
        EVENTS.add(new TrollEvent(id, weight, action));
    }

    static {
        // ---------- 嘴替类 ----------
        add("chat_spam", 20, (server, player) -> {
            ChaosUtil.msg(player, Msgs.pick(ChaosManager.RANDOM, Msgs.AMBIENT));
            return true;
        });

        add("fake_achievement", 8, (server, player) -> {
            ChaosUtil.broadcast(player, "🏆 " + player.getName().getString() + " 获得了成就：精神状态稳定！");
            ChaosUtil.sound(player, SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.6F);
            return true;
        });

        add("fake_death", 4, (server, player) -> {
            String name = player.getName().getString();
            ChaosUtil.broadcast(player, "☠ " + name + " 死了！");
            ChaosScheduler.schedule(server, 60, () -> ChaosUtil.broadcast(player, "……开个玩笑，" + name + " 还活着。惊不惊喜？"));
            return true;
        });

        add("system_glitch", 6, (server, player) -> {
            ChaosUtil.broadcast(player, "系统正在重新计算你的价值……计算结果：0。");
            ChaosScheduler.schedule(server, 40, () -> ChaosUtil.broadcast(player, "计算完成。请继续，反正也没人在意。"));
            return true;
        });

        add("villager_gossip", 6, (server, player) -> {
            ChaosUtil.broadcast(player, "村民们在背后议论你：『就是他，上次把村子的床全拆了。』");
            ChaosUtil.sound(player, SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 0.9F);
            return true;
        });

        // ---------- 背包类 ----------
        add("inventory_shuffle", 10, (server, player) -> {
            PlayerInventory inventory = player.getInventory();
            List<ItemStack> stacks = new ArrayList<>();
            for (int slot = 0; slot < 36; slot++) {
                stacks.add(inventory.getStack(slot).copy());
            }
            Collections.shuffle(stacks, ChaosManager.RANDOM);
            for (int slot = 0; slot < 36; slot++) {
                inventory.setStack(slot, stacks.get(slot));
            }
            ChaosUtil.sound(player, SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, 0.5F);
            ChaosUtil.msg(player, "你的背包被一阵妖风重新整理了。找东西？祝你好运。");
            return true;
        });

        add("item_steal", 7, (server, player) -> {
            PlayerInventory inventory = player.getInventory();
            List<Integer> filled = filledSlots(inventory);
            if (filled.isEmpty()) {
                return false;
            }
            int slot = filled.get(ChaosManager.RANDOM.nextInt(filled.size()));
            ItemStack stolen = inventory.getStack(slot).copy();
            if (stolen.isEmpty()) {
                return false;
            }
            String name = stolen.getName().getString();
            inventory.setStack(slot, ItemStack.EMPTY);
            ChaosUtil.msg(player, "一个看不见的贼偷走了你的 " + name + "！");
            ChaosScheduler.schedule(server, 200 + ChaosManager.RANDOM.nextInt(200), () -> {
                ChaosUtil.give(player, stolen);
                ChaosUtil.msg(player, "那个贼把 " + name + " 还回来了，还说了句『谢谢』。");
            });
            return true;
        });

        add("inventory_curse", 5, (server, player) -> {
            PlayerInventory inventory = player.getInventory();
            List<Integer> filled = filledSlots(inventory);
            if (filled.isEmpty()) {
                return false;
            }
            int slot = filled.get(ChaosManager.RANDOM.nextInt(filled.size()));
            ItemStack stack = inventory.getStack(slot);
            if (stack.isEmpty()) {
                return false;
            }
            String oldName = stack.getName().getString();
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("被诅咒的" + oldName).formatted(Formatting.DARK_PURPLE));
            ChaosUtil.msg(player, "你的 " + oldName + " 被诅咒了，它现在叫『被诅咒的" + oldName + "』。");
            return true;
        });

        add("hotbar_swap", 6, (server, player) -> {
            PlayerInventory inventory = player.getInventory();
            int selected = inventory.selectedSlot;
            int other = ChaosManager.RANDOM.nextInt(9);
            if (selected == other) {
                other = (other + 1) % 9;
            }
            ItemStack first = inventory.getStack(selected).copy();
            ItemStack second = inventory.getStack(other).copy();
            inventory.setStack(selected, second);
            inventory.setStack(other, first);
            ChaosUtil.msg(player, "你的手不听话了，它换了个东西拿。");
            return true;
        });

        add("drop_item", 4, (server, player) -> {
            ItemStack main = player.getMainHandStack();
            if (main.isEmpty()) {
                return false;
            }
            ItemStack drop = main.copy();
            player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
            player.dropItem(drop, false);
            ChaosUtil.msg(player, "你的手突然松开了。它说它不想拿这个。");
            return true;
        });

        add("gift", 9, (server, player) -> {
            ItemStack gift = switch (ChaosManager.RANDOM.nextInt(7)) {
                case 0 -> ChaosUtil.named(Items.ROTTEN_FLESH, "神秘礼物（已过期）", Formatting.DARK_GREEN);
                case 1 -> ChaosUtil.named(Items.POISONOUS_POTATO, "限量版土豆", Formatting.GREEN);
                case 2 -> ChaosUtil.named(Items.COBBLESTONE, "传家宝", Formatting.GRAY);
                case 3 -> ChaosUtil.named(Items.STICK, "神器（自称）", Formatting.GOLD);
                case 4 -> ChaosUtil.named(Items.DIRT, "极品泥土", Formatting.DARK_RED);
                case 5 -> ChaosUtil.named(Items.BONE, "某人的遗物", Formatting.WHITE);
                default -> ChaosUtil.named(Items.PUFFERFISH, "河豚（友情提示：别吃）", Formatting.YELLOW);
            };
            ChaosUtil.give(player, gift);
            ChaosUtil.msg(player, "一个神秘人往你手里塞了样东西，然后消失了。");
            return true;
        });

        add("fake_diamond", 8, (server, player) -> {
            ChaosUtil.give(player, ChaosUtil.named(Items.DIRT, "钻石（大概）", Formatting.GRAY));
            ChaosUtil.msg(player, "恭喜你挖到了钻石！请千万不要细看。");
            return true;
        });

        add("xp_blessing", 6, (server, player) -> {
            int xp = 1 + ChaosManager.RANDOM.nextInt(30);
            player.addExperience(xp);
            ChaosUtil.sound(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            ChaosUtil.msg(player, "你获得了 " + xp + " 点经验，但知识并没有增加。");
            return true;
        });

        // ---------- 身体类 ----------
        add("hunger_drain", 8, (server, player) -> {
            player.getHungerManager().addExhaustion(6.0F);
            ChaosUtil.msg(player, "你突然感到一阵空虚（物理意义上的）。");
            return true;
        });

        add("force_feed", 4, (server, player) -> {
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5.0F);
            ChaosUtil.msg(player, "你突然饱了，但你什么都没吃。建议不要细想。");
            return true;
        });

        add("levitation", 8, (server, player) -> {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 0));
            ChaosUtil.msg(player, "你飘了。（字面意思）");
            return true;
        });

        add("effect_roulette", 7, (server, player) -> {
            RegistryEntry<StatusEffect> effect = switch (ChaosManager.RANDOM.nextInt(5)) {
                case 0 -> StatusEffects.SPEED;
                case 1 -> StatusEffects.SLOWNESS;
                case 2 -> StatusEffects.JUMP_BOOST;
                case 3 -> StatusEffects.GLOWING;
                default -> StatusEffects.NAUSEA;
            };
            player.addStatusEffect(new StatusEffectInstance(effect, 200, ChaosManager.RANDOM.nextInt(2)));
            ChaosUtil.msg(player, "你的身体决定自己做主。祝你好运。");
            return true;
        });

        add("self_hurt", 5, (server, player) -> {
            player.damage(ChaosUtil.world(player).getDamageSources().magic(), 3.0F);
            ChaosUtil.sound(player, SoundEvents.ENTITY_PLAYER_HURT, 1.0F, 1.0F);
            ChaosUtil.msg(player, "你被自己的存在伤害了。");
            return true;
        });

        add("mob_buff", 6, (server, player) -> {
            List<LivingEntity> mobs = nearbyMobs(player);
            if (mobs.isEmpty()) {
                return false;
            }
            for (LivingEntity mob : mobs) {
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 1));
                mob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300, 0));
            }
            ChaosUtil.msg(player, "附近的生物突然都变得很兴奋，原因不明。");
            return true;
        });

        // ---------- 位移类 ----------
        add("small_teleport", 8, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            double x = player.getX() + ChaosUtil.offset(16.0);
            double z = player.getZ() + ChaosUtil.offset(16.0);
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
            ChaosUtil.teleport(player, x, y + 1, z);
            ChaosUtil.sound(player, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.2F);
            ChaosUtil.msg(player, "空间刚才卡了一下，把你挤到了别处。");
            return true;
        });

        add("spin", 5, (server, player) -> {
            float yaw = player.getYaw() + 90.0F + ChaosManager.RANDOM.nextFloat() * 180.0F;
            ChaosUtil.teleportRot(player, player.getX(), player.getY(), player.getZ(), yaw, player.getPitch());
            ChaosUtil.sound(player, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.5F);
            ChaosUtil.msg(player, "你的方向感被系统没收了。");
            return true;
        });

        // ---------- 生物类 ----------
        add("chicken_invasion", 7, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            for (int i = 0; i < 5; i++) {
                ChickenEntity chicken = EntityType.CHICKEN.create(world);
                if (chicken == null) {
                    break;
                }
                chicken.refreshPositionAndAngles(
                        player.getX() + ChaosUtil.offset(4.0),
                        player.getY() + 1.0,
                        player.getZ() + ChaosUtil.offset(4.0),
                        ChaosManager.RANDOM.nextFloat() * 360.0F, 0.0F);
                chicken.setCustomName(Text.literal("鸡你太美" + (i + 1)));
                chicken.setCustomNameVisible(true);
                chicken.setPersistent();
                world.spawnEntity(chicken);
            }
            ChaosUtil.broadcast(player, "有人在 " + player.getName().getString() + " 附近召唤了鸡群，原因不明。");
            return true;
        });

        add("pig_parade", 5, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            for (int i = 0; i < 3; i++) {
                PigEntity pig = EntityType.PIG.create(world);
                if (pig == null) {
                    break;
                }
                pig.refreshPositionAndAngles(
                        player.getX() + ChaosUtil.offset(5.0),
                        player.getY() + 1.0,
                        player.getZ() + ChaosUtil.offset(5.0),
                        ChaosManager.RANDOM.nextFloat() * 360.0F, 0.0F);
                pig.setCustomName(Text.literal("猪队友" + (i + 1) + "号"));
                pig.setCustomNameVisible(true);
                pig.setPersistent();
                world.spawnEntity(pig);
            }
            ChaosUtil.broadcast(player, "一群猪队友出现在 " + player.getName().getString() + " 身边。它们不打算帮忙。");
            return true;
        });

        add("mob_taunt", 9, (server, player) -> {
            List<LivingEntity> mobs = nearbyMobs(player);
            if (mobs.isEmpty()) {
                ChaosUtil.msg(player, "你觉得好像有人在背后说你坏话，但周围一个生物都没有。");
                return true;
            }
            LivingEntity mob = mobs.get(ChaosManager.RANDOM.nextInt(mobs.size()));
            ChaosUtil.broadcast(player, "【" + mob.getName().getString() + "】对 " + player.getName().getString()
                    + " 说：" + Msgs.pick(ChaosManager.RANDOM, Msgs.TAUNTS));
            ChaosUtil.sound(player, SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 0.8F);
            return true;
        });

        add("creeper_apology", 5, (server, player) -> {
            ChaosUtil.sound(player, SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 1.0F);
            ChaosUtil.msg(player, "苦力怕：对不起，走错了。");
            return true;
        });

        add("enderman_visitor", 4, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            EndermanEntity enderman = EntityType.ENDERMAN.create(world);
            if (enderman == null) {
                return false;
            }
            enderman.refreshPositionAndAngles(
                    player.getX() + ChaosUtil.offset(12.0),
                    player.getY() + 1.0,
                    player.getZ() + ChaosUtil.offset(12.0),
                    ChaosManager.RANDOM.nextFloat() * 360.0F, 0.0F);
            enderman.setCustomName(Text.literal("末影人（社恐）"));
            enderman.setCustomNameVisible(true);
            enderman.setAiDisabled(true);
            enderman.setPersistent();
            world.spawnEntity(enderman);
            ChaosUtil.sound(player, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.8F);
            ChaosUtil.msg(player, "一只社恐末影人出现在附近。它不想说话，也不想动。");
            return true;
        });

        // ---------- 环境类 ----------
        add("tnt_stalker", 4, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            double angle = ChaosManager.RANDOM.nextDouble() * Math.PI * 2.0;
            double x = player.getX() + Math.cos(angle) * 4.0;
            double z = player.getZ() + Math.sin(angle) * 4.0;
            TntEntity tnt = new TntEntity(world, x, player.getY() + 1.0, z, null);
            tnt.setFuse(80);
            world.spawnEntity(tnt);
            ChaosUtil.sound(player, SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 1.0F);
            ChaosUtil.msg(player, "你背后……好像有什么东西。建议跑。");
            return true;
        });

        add("diamond_rain", 5, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            for (int i = 0; i < 5; i++) {
                ItemStack stack = ChaosManager.RANDOM.nextBoolean()
                        ? new ItemStack(Items.DIAMOND)
                        : ChaosUtil.named(Items.DIRT, "假钻石", Formatting.GRAY);
                ItemEntity item = new ItemEntity(world,
                        player.getX() + ChaosUtil.offset(3.0),
                        player.getY() + 6.0,
                        player.getZ() + ChaosUtil.offset(3.0),
                        stack);
                item.setVelocity(ChaosUtil.offset(0.2), 0.2, ChaosUtil.offset(0.2));
                world.spawnEntity(item);
            }
            ChaosUtil.sound(player, SoundEvents.ENTITY_PLAYER_LEVELUP, 0.7F, 1.5F);
            ChaosUtil.msg(player, "天上下钻石了！其中大约一半是真的，另一半是土。");
            return true;
        });

        add("weather_chaos", 4, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            boolean thunder = ChaosManager.RANDOM.nextBoolean();
            world.setWeather(thunder ? 0 : 6000, thunder ? 6000 : 12000, true, thunder);
            ChaosUtil.broadcast(player, "天气预报：今天有 100% 的概率下点什么，具体下什么由服务器决定。");
            return true;
        });

        add("time_chaos", 4, (server, player) -> {
            ChaosUtil.world(player).setTimeOfDay(ChaosManager.RANDOM.nextInt(24000));
            ChaosUtil.broadcast(player, "时间表示：它裂开了。");
            return true;
        });

        add("spooky_sound", 8, (server, player) -> {
            switch (ChaosManager.RANDOM.nextInt(6)) {
                case 0 -> ChaosUtil.sound(player, SoundEvents.ENTITY_GHAST_SCREAM, 1.0F, 0.7F);
                case 1 -> ChaosUtil.sound(player, SoundEvents.ENTITY_WITHER_SPAWN, 0.8F, 0.6F);
                case 2 -> ChaosUtil.sound(player, SoundEvents.ENTITY_ENDERMAN_SCREAM, 1.0F, 0.9F);
                case 3 -> ChaosUtil.sound(player, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 1.0F, 0.8F);
                case 4 -> ChaosUtil.sound(player, SoundEvents.BLOCK_ANVIL_LAND, 1.0F, 0.6F);
                default -> ChaosUtil.sound(player, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8F, 1.0F);
            }
            ChaosUtil.msg(player, "你听到了不该听到的声音。");
            return true;
        });

        add("lightning_tease", 4, (server, player) -> {
            ChaosUtil.sound(player, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.2F);
            ChaosUtil.msg(player, "差一点。");
            return true;
        });

        add("shadow_follow", 4, (server, player) -> {
            ChaosUtil.sound(player, SoundEvents.ENTITY_ENDERMAN_STARE, 0.8F, 0.7F);
            ChaosUtil.particle(player, net.minecraft.particle.ParticleTypes.PORTAL, 24);
            ChaosUtil.msg(player, "你总觉得有什么东西跟着你……别回头，真的。");
            return true;
        });

        // ---------- 丧心病狂类 ----------
        add("total_chaos", 2, (server, player) -> {
            ChaosUtil.broadcast(player, "【系统】检测到你过于快乐，已为你安排一点挫折。");
            ChaosManager.fireRandom(server, player);
            ChaosManager.fireRandom(server, player);
            return true;
        });

        add("chat_takeover", 3, (server, player) -> {
            String name = player.getName().getString();
            ChaosScheduler.schedule(server, 0, () -> ChaosUtil.broadcast(player, "<" + name + "> 我没说话。"));
            ChaosScheduler.schedule(server, 30, () -> ChaosUtil.broadcast(player, "<" + name + "> 我真的没说话。"));
            ChaosScheduler.schedule(server, 60, () -> ChaosUtil.broadcast(player, "<" + name + "> 好吧是我说的。"));
            return true;
        });

        // ---------- 新增：更不做人的 ----------
        add("neck_crack", 6, (server, player) -> {
            float pitch = -90.0F + ChaosManager.RANDOM.nextFloat() * 180.0F;
            float yaw = (float) (player.getYaw() + ChaosUtil.offset(30.0));
            ChaosUtil.teleportRot(player, player.getX(), player.getY(), player.getZ(), yaw, pitch);
            ChaosUtil.sound(player, SoundEvents.ENTITY_SKELETON_HURT, 0.8F, 1.4F);
            ChaosUtil.msg(player, "你的脖子刚刚自己转了一下。它现在很满意。");
            return true;
        });

        add("anvil_prank", 4, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            FallingBlockEntity.spawnFromBlock(world, BlockPos.ofFloored(
                    player.getX() + ChaosUtil.offset(1.5),
                    player.getY() + 8.0,
                    player.getZ() + ChaosUtil.offset(1.5)),
                    Blocks.ANVIL.getDefaultState());
            ChaosUtil.sound(player, SoundEvents.BLOCK_ANVIL_LAND, 1.0F, 0.8F);
            ChaosUtil.msg(player, "小心头顶。");
            return true;
        });

        add("fake_ore", 6, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            Map<BlockPos, BlockState> replaced = new LinkedHashMap<>();
            BlockPos center = player.getBlockPos();
            for (int i = 0; i < 4; i++) {
                BlockPos pos = center.add(ChaosManager.RANDOM.nextInt(9) - 4,
                        ChaosManager.RANDOM.nextInt(3) - 1,
                        ChaosManager.RANDOM.nextInt(9) - 4);
                BlockState state = world.getBlockState(pos);
                if (state.isOf(Blocks.STONE) || state.isOf(Blocks.DEEPSLATE) || state.isOf(Blocks.COBBLESTONE)) {
                    replaced.put(pos, state);
                }
            }
            if (replaced.isEmpty()) {
                return false;
            }
            for (BlockPos pos : replaced.keySet()) {
                world.setBlockState(pos, ChaosManager.RANDOM.nextBoolean()
                        ? Blocks.DIAMOND_ORE.getDefaultState()
                        : Blocks.DEEPSLATE_DIAMOND_ORE.getDefaultState());
            }
            ChaosUtil.sound(player, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 0.6F);
            ChaosUtil.msg(player, "你发现了钻石矿！……可惜它们只存在 3 秒。");
            ChaosScheduler.schedule(server, 60, () -> {
                for (Map.Entry<BlockPos, BlockState> entry : replaced.entrySet()) {
                    world.setBlockState(entry.getKey(), entry.getValue());
                }
            });
            return true;
        });

        add("ice_floor", 5, (server, player) -> {
            ServerWorld world = ChaosUtil.world(player);
            BlockPos below = player.getBlockPos().down();
            BlockState state = world.getBlockState(below);
            if (state.isAir()) {
                return false;
            }
            world.setBlockState(below, Blocks.ICE.getDefaultState());
            ChaosUtil.sound(player, SoundEvents.BLOCK_GLASS_PLACE, 1.0F, 1.6F);
            ChaosUtil.msg(player, "你脚下的地面突然变得很滑。这和你没关系，别多想。");
            return true;
        });

        add("fake_player_chat", 6, (server, player) -> {
            String[] names = {"Notch", "Herobrine", "Steve", "Alex", "村民老王", "一只鸡"};
            ChaosUtil.broadcast(player, "<" + names[ChaosManager.RANDOM.nextInt(names.length)] + "> "
                    + Msgs.pick(ChaosManager.RANDOM, Msgs.FAKE_CHAT));
            return true;
        });

        add("fake_friend_join", 4, (server, player) -> {
            String[] names = {"Steve", "Alex", "Herobrine", "Notch"};
            String name = names[ChaosManager.RANDOM.nextInt(names.length)];
            ChaosUtil.broadcast(player, name + " 加入了游戏");
            ChaosScheduler.schedule(server, 100, () -> ChaosUtil.broadcast(player, name + " 退出了游戏"));
            return true;
        });

        add("swap_players", 4, (server, player) -> {
            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
            if (players.size() < 2) {
                return false;
            }
            ServerPlayerEntity other = players.get(ChaosManager.RANDOM.nextInt(players.size()));
            if (other == player) {
                other = players.get((players.indexOf(player) + 1) % players.size());
            }
            if (other.getServerWorld() != player.getServerWorld()) {
                return false;
            }
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            ChaosUtil.teleport(player, other.getX(), other.getY(), other.getZ());
            ChaosUtil.teleport(other, x, y, z);
            ChaosUtil.broadcast(player, "两位玩家的位置刚刚被交换了，没有原因，也不需要原因。");
            return true;
        });

        add("totem_tease", 5, (server, player) -> {
            PlayerInventory inventory = player.getInventory();
            boolean hasTotem = false;
            for (int slot = 0; slot < 41; slot++) {
                if (inventory.getStack(slot).isOf(Items.TOTEM_OF_UNDYING)) {
                    hasTotem = true;
                    break;
                }
            }
            if (hasTotem) {
                ChaosUtil.msg(player, "你的不死图腾刚刚抖了一下。它说它今天不想上班。");
            } else {
                ChaosUtil.msg(player, "你没有不死图腾。现在你知道为什么了。");
            }
            return true;
        });
    }

    public static List<TrollEvent> all() {
        return Collections.unmodifiableList(EVENTS);
    }

    public static List<String> ids() {
        List<String> ids = new ArrayList<>(EVENTS.size());
        for (TrollEvent event : EVENTS) {
            ids.add(event.id());
        }
        return ids;
    }

    public static TrollEvent byId(String id) {
        for (TrollEvent event : EVENTS) {
            if (event.id().equals(id)) {
                return event;
            }
        }
        return null;
    }

    public static TrollEvent pick(java.util.Random random) {
        int total = 0;
        for (TrollEvent event : EVENTS) {
            total += event.weight();
        }
        int roll = random.nextInt(total);
        for (TrollEvent event : EVENTS) {
            roll -= event.weight();
            if (roll < 0) {
                return event;
            }
        }
        return EVENTS.get(EVENTS.size() - 1);
    }

    private static List<Integer> filledSlots(PlayerInventory inventory) {
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            if (!inventory.getStack(slot).isEmpty()) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private static List<LivingEntity> nearbyMobs(ServerPlayerEntity player) {
        ServerWorld world = ChaosUtil.world(player);
        return world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(16.0),
                entity -> !(entity instanceof PlayerEntity) && entity.isAlive());
    }
}