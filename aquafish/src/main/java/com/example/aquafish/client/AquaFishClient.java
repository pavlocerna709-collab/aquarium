package com.example.aquafish.client;

import com.example.aquafish.block.ModBlockEntities;
import com.example.aquafish.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class AquaFishClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.AQUARIUM, RenderLayer.getTranslucent());
		BlockEntityRendererFactories.register(ModBlockEntities.AQUARIUM, AquariumRenderer::new);
	}
}
