package com.example.aquafish.item;

import com.example.aquafish.AquaFish;
import com.example.aquafish.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class ModItems {
	public static final Item FISH_FOOD = Registry.register(Registries.ITEM, AquaFish.id("fish_food"),
			new Item(new Item.Settings()));
	public static final Item AQUARIUM = Registry.register(Registries.ITEM, AquaFish.id("aquarium"),
			new BlockItem(ModBlocks.AQUARIUM, new Item.Settings()));

	public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, AquaFish.id("main"),
			FabricItemGroup.builder()
					.icon(() -> new ItemStack(AQUARIUM))
					.displayName(Text.translatable("itemGroup.aquafish"))
					.entries((ctx, entries) -> {
						entries.add(AQUARIUM);
						entries.add(FISH_FOOD);
					})
					.build());

	public static void init() {
	}
}
