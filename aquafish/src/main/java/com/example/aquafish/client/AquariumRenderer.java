package com.example.aquafish.client;

import com.example.aquafish.block.AquariumBlockEntity;
import com.example.aquafish.fish.FishType;
import com.example.aquafish.item.ModItems;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Рисует плавающих рыбок и анимацию кормления:
 *  1) баночка с кормом появляется над аквариумом, наклоняется и трясётся;
 *  2) из неё сыпятся разноцветные хлопья, разлетаются и тонут;
 *  3) рыбки всплывают к поверхности и собираются под кормушкой.
 */
public class AquariumRenderer implements BlockEntityRenderer<AquariumBlockEntity> {
	/** Если рыбки плывут хвостом вперёд — поменяй на 0f. */
	private static final float HEADING_OFFSET = 180f;
	private static final float FISH_SCALE = 0.36f;

	private final ItemRenderer itemRenderer;
	private final ItemStack foodStack = new ItemStack(ModItems.FISH_FOOD);
	private final Map<FishType, ItemStack> fishStacks = new EnumMap<>(FishType.class);
	private final ItemStack[] flakes = {
			new ItemStack(Items.ORANGE_DYE), new ItemStack(Items.RED_DYE),
			new ItemStack(Items.YELLOW_DYE), new ItemStack(Items.LIME_DYE)
	};

	public AquariumRenderer(BlockEntityRendererFactory.Context ctx) {
		this.itemRenderer = ctx.getItemRenderer();
		for (FishType t : FishType.values()) fishStacks.put(t, new ItemStack(t.model));
	}

	@Override
	public boolean rendersOutsideBoundingBox(AquariumBlockEntity be) {
		return true; // баночка с кормом висит над блоком
	}

	@Override
	public void render(AquariumBlockEntity be, float tickDelta, MatrixStack m, VertexConsumerProvider vc,
					   int light, int overlay) {
		World world = be.getWorld();
		if (world == null) return;

		float time = world.getTime() + tickDelta;
		float feedT = time - be.getFeedStart();
		boolean feeding = feedT >= 0 && feedT < AquariumBlockEntity.FEED_ANIMATION_TICKS;
		float w = feeding ? feedWeight(feedT) : 0f;

		List<FishType> fish = be.getFish();
		for (int i = 0; i < fish.size(); i++) {
			renderFish(fish.get(i), i, time, w, m, vc, light, overlay, world);
		}

		if (feeding) {
			renderCan(feedT, m, vc, world);
			renderFlakes(feedT, m, vc, world);
		}
	}

	// ---------- рыбки ----------

	private void renderFish(FishType type, int i, float time, float w, MatrixStack m, VertexConsumerProvider vc,
							int light, int overlay, World world) {
		float dir = (i % 2 == 0) ? 1f : -1f;
		float seed = i * 2.1f + type.ordinal() * 0.7f;
		float a = dir * time * 0.04f * type.speed + seed;
		float rx = 0.24f + 0.05f * (i % 3);
		float rz = 0.24f + 0.05f * ((i + 1) % 3);

		float x = 0.5f + rx * MathHelper.cos(a);
		float z = 0.5f + rz * MathHelper.sin(a);
		float y = type.depth + 0.07f * MathHelper.sin(time * 0.06f + seed) + (i % 3) * 0.04f;

		float dx = -rx * MathHelper.sin(a) * dir;
		float dz = rz * MathHelper.cos(a) * dir;
		float heading = (float) Math.toDegrees(Math.atan2(dz, dx));

		// во время кормления: всплывают и собираются под кормушкой
		if (w > 0f) {
			float tx = 0.5f + 0.13f * MathHelper.cos(time * 0.25f + seed * 3f);
			float tz = 0.5f + 0.13f * MathHelper.sin(time * 0.25f + seed * 3f);
			x = MathHelper.lerp(w * 0.75f, x, tx);
			z = MathHelper.lerp(w * 0.75f, z, tz);
			y = MathHelper.lerp(w, y, 0.70f + 0.04f * MathHelper.sin(time * 0.5f + seed));
		}

		float wiggle = 8f * MathHelper.sin(time * 0.45f * type.speed + seed);

		m.push();
		m.translate(x, y, z);
		m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-heading + HEADING_OFFSET + wiggle));
		m.scale(FISH_SCALE, FISH_SCALE, FISH_SCALE);
		itemRenderer.renderItem(fishStacks.get(type), ModelTransformationMode.FIXED, light, overlay, m, vc, world, i);
		m.pop();
	}

	private static float feedWeight(float t) {
		if (t < 15f) return t / 15f;
		if (t > 60f) return Math.max(0f, (80f - t) / 20f);
		return 1f;
	}

	// ---------- баночка с кормом ----------

	private void renderCan(float t, MatrixStack m, VertexConsumerProvider vc, World world) {
		if (t > 50f) return;
		float tilt = t < 10f ? t / 10f : 1f;
		float shake = t >= 10f ? MathHelper.sin(t * 1.8f) * 7f : 0f;
		float s = 0.6f * (t < 5f ? t / 5f : (t > 42f ? (50f - t) / 8f : 1f));

		m.push();
		m.translate(0.5, 1.3, 0.5);
		m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-110f * tilt + shake));
		m.scale(s, s, s);
		itemRenderer.renderItem(foodStack, ModelTransformationMode.GROUND,
				LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, m, vc, world, 0);
		m.pop();
	}

	// ---------- сыплющиеся хлопья ----------

	private void renderFlakes(float t, MatrixStack m, VertexConsumerProvider vc, World world) {
		final int count = 40;
		for (int k = 0; k < count; k++) {
			float h1 = hash(k * 3 + 1), h2 = hash(k * 3 + 2), h3 = hash(k * 3 + 3);
			float delay = 6f + h1 * 28f;       // начинают сыпаться, когда баночка уже наклонилась
			float p = (t - delay) / 42f;       // 0..1 — путь от баночки до дна
			if (p < 0f || p > 1f) continue;

			float spread = (0.03f + 0.30f * p) * h3;
			float ang = h2 * MathHelper.TAU;
			float x = 0.5f + MathHelper.cos(ang) * spread + MathHelper.sin(t * 0.3f + k) * 0.015f;
			float z = 0.5f + MathHelper.sin(ang) * spread;
			float y = 1.2f - 0.9f * p;          // с 1.2 (над блоком) до ~0.3 (у дна)

			m.push();
			m.translate(x, y, z);
			m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(t * 25f + k * 40f));
			m.scale(0.09f, 0.09f, 0.09f);
			itemRenderer.renderItem(flakes[k % flakes.length], ModelTransformationMode.FIXED,
					LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, m, vc, world, k);
			m.pop();
		}
	}

	private static float hash(int n) {
		int x = n * 374761393;
		x = (x ^ (x >>> 13)) * 1274126177;
		x ^= x >>> 16;
		return (x & 0xFFFF) / 65535f;
	}
}
