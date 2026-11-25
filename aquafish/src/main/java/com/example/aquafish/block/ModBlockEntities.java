package com.example.aquafish.block;

import com.example.aquafish.AquaFish;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
	public static final BlockEntityType<AquariumBlockEntity> AQUARIUM = Registry.register(
			Registries.BLOCK_ENTITY_TYPE, AquaFish.id("aquarium"),
			BlockEntityType.Builder.create(AquariumBlockEntity::new, ModBlocks.AQUARIUM).build());

	public static void init() {
	}
}
