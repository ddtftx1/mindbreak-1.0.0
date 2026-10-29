package com.mindbreak.command;

import com.mindbreak.chaos.ChaosEvents;
import com.mindbreak.chaos.ChaosManager;
import com.mindbreak.chaos.ChaosUtil;
import com.mindbreak.item.ModItems;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * /mindbreak 命令。没有权限限制，因为快乐不应该被权限限制。
 */
public final class MindbreakCommand {
    private MindbreakCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("mindbreak")
                        .executes(context -> {
                            sendHelp(context.getSource());
                            return 1;
                        })
                        .then(CommandManager.literal("help").executes(context -> {
                            sendHelp(context.getSource());
                            return 1;
                        }))
                        .then(CommandManager.literal("toggle").executes(context -> {
                            ChaosManager.setEnabled(!ChaosManager.isEnabled());
                            boolean now = ChaosManager.isEnabled();
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "【精神污染】整活已" + (now ? "开启" : "关闭") + "。" + (now ? "祝你好运。" : "你以为这就结束了？")), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("intensity")
                                .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 3)).executes(context -> {
                                    int level = IntegerArgumentType.getInteger(context, "level");
                                    ChaosManager.setIntensity(level);
                                    String comment = switch (level) {
                                        case 1 -> "温柔模式，只是偶尔搞你一下。";
                                        case 2 -> "正常模式，建议保持警惕。";
                                        default -> "不做人模式，出事了别找我。";
                                    };
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "【精神污染】整活强度已设为 " + level + "：" + comment), false);
                                    return 1;
                                })))
                        .then(CommandManager.literal("trigger")
                                .then(CommandManager.argument("event", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            ChaosEvents.ids().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String id = StringArgumentType.getString(context, "event");
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            if (player == null) {
                                                context.getSource().sendError(Text.literal("这个命令必须由玩家执行。"));
                                                return 0;
                                            }
                                            boolean happened = ChaosManager.fireById(id, context.getSource().getServer(), player);
                                            if (happened) {
                                                context.getSource().sendFeedback(() -> Text.literal(
                                                        "【精神污染】已触发事件：" + id), false);
                                                return 1;
                                            }
                                            context.getSource().sendError(Text.literal(
                                                    "事件不存在，或者它这次不想搞你：" + id));
                                            return 0;
                                        })))
                        .then(CommandManager.literal("list").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "【精神污染】共 " + ChaosEvents.all().size() + " 个事件：" + String.join(", ", ChaosEvents.ids())), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("gift").executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) {
                                context.getSource().sendError(Text.literal("这个命令必须由玩家执行。"));
                                return 0;
                            }
                            ChaosUtil.give(player, new ItemStack(ModItems.ABSURD_STICK));
                            ChaosUtil.give(player, new ItemStack(ModItems.CURSE_BOOK));
                            ChaosUtil.give(player, new ItemStack(ModItems.CHAOS_PEARL));
                            ChaosUtil.give(player, new ItemStack(ModItems.REGRET_CAKE, 3));
                            ChaosUtil.give(player, new ItemStack(ModItems.MYSTERY_MEAT, 3));
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "【精神污染】五件套已发放。使用后果自负，其实作者也不负责。"), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("chaos").executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) {
                                context.getSource().sendError(Text.literal("这个命令必须由玩家执行。"));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "【精神污染】五连发，请站稳。"), false);
                            for (int i = 0; i < 5; i++) {
                                ChaosManager.fireRandom(context.getSource().getServer(), player);
                            }
                            return 1;
                        }))
                        .then(CommandManager.literal("stats").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "【精神污染】状态：" + (ChaosManager.isEnabled() ? "开着" : "关着")
                                            + "，强度 " + ChaosManager.getIntensity()
                                            + "，已累计整活 " + ChaosManager.getEventsFired() + " 次。"), false);
                            return 1;
                        }))));
    }

    private static void sendHelp(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("""
                【精神污染 Mindbreak】命令列表：
                /mindbreak toggle            开启或关闭整活
                /mindbreak intensity <1-3>   设置强度
                /mindbreak trigger <事件id>  立刻触发一个事件
                /mindbreak list              列出全部事件
                /mindbreak gift              领取五件神器
                /mindbreak chaos             随机五连发
                /mindbreak stats             查看状态
                提示：这个模组不会保护你的存档，也不会保护你的精神状态。"""), false);
    }
}