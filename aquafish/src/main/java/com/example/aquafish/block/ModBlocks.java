package com.example.aquafish.block;

import com.example.aquafish.AquaFish;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlocks {
	public static final Block AQUARIUM = Registry.register(Registries.BLOCK, AquaFish.id("aquarium"),
			new AquariumBlock(AbstractBlock.Settings.copy(Blocks.GLASS)));

	public static void init() {
	}
}
