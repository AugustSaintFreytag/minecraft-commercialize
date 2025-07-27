package net.saint.commercialize.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

/**
 * Abstract base class for blocks that span two vertical blocks (double height).
 * Handles common twin-block logic such as placement, breaking, and interaction forwarding.
 */
public abstract class DoubleBlockWithEntity extends BlockWithEntity {

	// Library

	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

	// Init

	public DoubleBlockWithEntity(Settings settings) {
		super(settings);
	}

	// Entity

	@Override
	public abstract BlockEntity createBlockEntity(BlockPos position, BlockState blockState);

	// Placement

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		var blockPos = context.getBlockPos();
		var world = context.getWorld();

		if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(context)) {
			var baseState = getMasterPlacementState(context);
			return baseState != null ? baseState.with(HALF, DoubleBlockHalf.LOWER) : null;
		} else {
			return null;
		}
	}

	protected abstract BlockState getMasterPlacementState(ItemPlacementContext context);

	@Override
	public void onPlaced(World world, BlockPos position, BlockState state, LivingEntity placer, ItemStack itemStack) {
		world.setBlockState(position.up(), state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
		super.onPlaced(world, position, state, placer, itemStack);
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(HALF);
		super.appendProperties(builder);
	}

	// Mutation

	@Override
	public void onBreak(World world, BlockPos position, BlockState state, PlayerEntity player) {
		var twinBlockPosition = getOtherHalfBlockPosition(position, state);
		var twinBlockState = world.getBlockState(twinBlockPosition);

		if (twinBlockState.getBlock() == this) {
			world.setBlockState(twinBlockPosition, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
			world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, twinBlockPosition, Block.getRawIdFromState(twinBlockState));
		}

		super.onBreak(world, position, state, player);
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos position, BlockState newState, boolean moved) {
		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			return;
		}

		if (state.getBlock() == newState.getBlock()) {
			return;
		}

		onStateReplacedLowerHalf(state, world, position, newState, moved);
	}

	protected abstract void onStateReplacedLowerHalf(BlockState state, World world, BlockPos position, BlockState newState, boolean moved);

	// Interaction

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos position, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient() || hand == Hand.OFF_HAND) {
			return ActionResult.CONSUME;
		}

		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			// Interaction with upper half is deferred to lower half instead.
			var lowerHalfPosition = position.down();
			var lowerHalfBlockState = world.getBlockState(lowerHalfPosition);

			if (lowerHalfBlockState.getBlock() instanceof DoubleBlockWithEntity lowerHalfBlock) {
				return lowerHalfBlock.onMasterBlockUse(lowerHalfBlockState, world, lowerHalfPosition, player, hand, hit);
			}

			return ActionResult.CONSUME;
		}

		return onMasterBlockUse(state, world, position, player, hand, hit);
	}

	protected abstract ActionResult onMasterBlockUse(BlockState state, World world, BlockPos position, PlayerEntity player, Hand hand,
			BlockHitResult hit);

	// Rendering

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	public long getRenderingSeed(BlockState state, BlockPos position) {
		var x = position.getX();
		var y = position.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY();
		var z = position.getZ();

		return new BlockPos(x, y, z).hashCode();
	}

	// Utility

	protected BlockPos getOtherHalfBlockPosition(BlockPos position, BlockState state) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			return position.up();
		} else {
			return position.down();
		}
	}

}
