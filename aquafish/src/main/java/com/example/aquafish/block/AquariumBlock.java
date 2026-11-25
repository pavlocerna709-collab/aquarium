package com.example.aquafish.block;

import com.example.aquafish.fish.FishType;
import com.example.aquafish.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AquariumBlock extends BlockWithEntity {
	public static final MapCodec<AquariumBlock> CODEC = createCodec(AquariumBlock::new);

	public AquariumBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new AquariumBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModBlockEntities.AQUARIUM, AquariumBlockEntity::tick);
	}

	/** ПКМ предметом: ведро с рыбой — посадить, пустое ведро — достать, корм — покормить. */
	@Override
	public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
										  PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof AquariumBlockEntity aq)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}

		// 1) посадить рыбку
		FishType type = FishType.fromBucket(stack.getItem());
		if (type != null) {
			if (world.isClient) return ItemActionResult.SUCCESS;
			if (!aq.addFish(type)) {
				player.sendMessage(Text.translatable("message.aquafish.full"), true);
				return ItemActionResult.CONSUME;
			}
			player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
			world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_FISH, SoundCategory.BLOCKS, 1f, 1f);
			return ItemActionResult.SUCCESS;
		}

		// 2) достать рыбку пустым ведром
		if (stack.isOf(Items.BUCKET)) {
			if (aq.getFish().isEmpty()) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (world.isClient) return ItemActionResult.SUCCESS;
			FishType removed = aq.removeLast();
			if (removed != null) {
				player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(removed.bucket)));
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_FISH, SoundCategory.BLOCKS, 1f, 1f);
			}
			return ItemActionResult.SUCCESS;
		}

		// 3) покормить
		if (stack.isOf(ModItems.FISH_FOOD)) {
			if (world.isClient) return ItemActionResult.SUCCESS;
			if (aq.getFish().isEmpty()) {
				player.sendMessage(Text.translatable("message.aquafish.no_fish"), true);
				return ItemActionResult.CONSUME;
			}
			if (aq.isFed()) {
				player.sendMessage(Text.translatable("message.aquafish.not_hungry"), true);
				return ItemActionResult.CONSUME;
			}
			aq.feed();
			stack.decrementUnlessCreative(1, player);
			world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 1f, 1.5f);
			return ItemActionResult.SUCCESS;
		}

		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	/** ПКМ пустой рукой — показать, сколько рыбок живёт в аквариуме. */
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof AquariumBlockEntity aq) {
			player.sendMessage(Text.translatable("message.aquafish.count",
					aq.getFish().size(), AquariumBlockEntity.MAX_FISH), true);
		}
		return ActionResult.SUCCESS;
	}

	/** При разрушении рыбки возвращаются в виде вёдер. */
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!world.isClient && !state.isOf(newState.getBlock())
				&& world.getBlockEntity(pos) instanceof AquariumBlockEntity aq) {
			for (FishType t : aq.getFish()) {
				ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(t.bucket));
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
