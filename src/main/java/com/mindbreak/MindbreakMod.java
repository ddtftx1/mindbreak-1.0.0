package com.mindbreak;

import com.mindbreak.callback.Callbacks;
import com.mindbreak.command.MindbreakCommand;
import com.mindbreak.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 精神污染 Mindbreak —— 一个不崩游戏、只崩你的模组。
 */
public class MindbreakMod implements ModInitializer {
    public static final String MOD_ID = "mindbreak";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.register();
        Callbacks.register();
        MindbreakCommand.register();
        LOGGER.info("[精神污染] 已加载。祝你好运，你会的。");
    }
}