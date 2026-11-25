package com.example.aquafish;

import com.example.aquafish.block.ModBlockEntities;
import com.example.aquafish.block.ModBlocks;
import com.example.aquafish.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class AquaFish implements ModInitializer {
	public static final String MOD_ID = "aquafish";

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.init();
		ModItems.init();
		ModBlockEntities.init();
	}
}
