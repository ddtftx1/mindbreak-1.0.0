package com.mindbreak.item;

import java.util.function.Function;

import com.mindbreak.MindbreakMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * 五件神器。都是限量的（其实不是）。
 */
public final class ModItems {
    public static final Item ABSURD_STICK = register("absurd_stick", AbsurdStickItem::new,
            new Item.Settings().maxCount(1).rarity(Rarity.EPIC));

    public static final Item CURSE_BOOK = register("curse_book", CurseBookItem::new,
            new Item.Settings().maxCount(1).rarity(Rarity.RARE));

    public static final Item CHAOS_PEARL = register("chaos_pearl", ChaosPearlItem::new,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE));

    public static final Item REGRET_CAKE = register("regret_cake", RegretCakeItem::new,
            new Item.Settings().food(new FoodComponent.Builder()
                    .nutrition(2)
                    .saturationModifier(0.05F)
                    .alwaysEdible()
                    .build()));

    public static final Item MYSTERY_MEAT = register("mystery_meat", MysteryMeatItem::new,
            new Item.Settings().food(new FoodComponent.Builder()
                    .nutrition(3)
                    .saturationModifier(0.1F)
                    .alwaysEdible()
                    .build()));

    private ModItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(ABSURD_STICK);
            entries.add(CURSE_BOOK);
            entries.add(CHAOS_PEARL);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(REGRET_CAKE);
            entries.add(MYSTERY_MEAT);
        });
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MindbreakMod.MOD_ID, name));
        return Registry.register(Registries.ITEM, key, factory.apply(settings));
    }
}