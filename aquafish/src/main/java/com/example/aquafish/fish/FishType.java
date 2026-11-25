package com.example.aquafish.fish;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

/** Рыбки, которые можно посадить в аквариум (по вёдрам из ванильной игры). */
public enum FishType {
	SALMON(Items.SALMON_BUCKET, Items.SALMON, 1.0f, 0.55f),
	COD(Items.COD_BUCKET, Items.COD, 0.85f, 0.40f),
	CLOWNFISH(Items.TROPICAL_FISH_BUCKET, Items.TROPICAL_FISH, 1.25f, 0.62f), // «Немо»
	PUFFERFISH(Items.PUFFERFISH_BUCKET, Items.PUFFERFISH, 0.6f, 0.33f);

	public final Item bucket;
	/** Предмет, спрайт которого рисуется как плавающая рыбка. */
	public final Item model;
	public final float speed;
	/** На какой высоте (0..1 внутри блока) рыбка предпочитает плавать. */
	public final float depth;

	FishType(Item bucket, Item model, float speed, float depth) {
		this.bucket = bucket;
		this.model = model;
		this.speed = speed;
		this.depth = depth;
	}

	@Nullable
	public static FishType fromBucket(Item item) {
		for (FishType t : values()) {
			if (t.bucket == item) return t;
		}
		return null;
	}

	public static FishType byId(int id) {
		FishType[] v = values();
		return v[Math.floorMod(id, v.length)];
	}
}
