package com.example.aquafish.block;

import com.example.aquafish.fish.FishType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AquariumBlockEntity extends BlockEntity {
	public static final int MAX_FISH = 6;
	/** Длительность анимации высыпания корма (тики). */
	public static final int FEED_ANIMATION_TICKS = 80;
	/** Сколько рыбки остаются сытыми (тики, 3 минуты). */
	public static final long FED_DURATION = 3600L;

	private final List<FishType> fish = new ArrayList<>();
	private long feedStart = -1_000_000L;
	private long fedUntil = 0L;

	public AquariumBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.AQUARIUM, pos, state);
	}

	public List<FishType> getFish() {
		return Collections.unmodifiableList(fish);
	}

	public long getFeedStart() {
		return feedStart;
	}

	public boolean isFull() {
		return fish.size() >= MAX_FISH;
	}

	public boolean isFed() {
		return world != null && world.getTime() < fedUntil;
	}

	public boolean addFish(FishType type) {
		if (isFull()) return false;
		fish.add(type);
		sync();
		return true;
	}

	@Nullable
	public FishType removeLast() {
		if (fish.isEmpty()) return null;
		FishType t = fish.remove(fish.size() - 1);
		sync();
		return t;
	}

	/** Запускает анимацию кормления (клиент считает её по feedStart). */
	public void feed() {
		if (world == null) return;
		feedStart = world.getTime();
		fedUntil = feedStart + FED_DURATION;
		sync();
	}

	private void sync() {
		markDirty();
		if (world != null) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
		}
	}

	public static void tick(World world, BlockPos pos, BlockState state, AquariumBlockEntity be) {
		// Сытые рыбки изредка «радуются»
		if (world instanceof ServerWorld sw && !be.fish.isEmpty() && world.getTime() < be.fedUntil
				&& world.getTime() % 40 == 0) {
			sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
					2, 0.25, 0.05, 0.25, 0.0);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		nbt.putIntArray("Fish", fish.stream().mapToInt(Enum::ordinal).toArray());
		nbt.putLong("FeedStart", feedStart);
		nbt.putLong("FedUntil", fedUntil);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		fish.clear();
		for (int id : nbt.getIntArray("Fish")) {
			if (fish.size() < MAX_FISH) fish.add(FishType.byId(id));
		}
		feedStart = nbt.contains("FeedStart") ? nbt.getLong("FeedStart") : -1_000_000L;
		fedUntil = nbt.getLong("FedUntil");
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
		return createNbt(lookup);
	}
}
