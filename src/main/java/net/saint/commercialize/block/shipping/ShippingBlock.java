package net.saint.commercialize.block.shipping;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.init.ModBlockEntities;

public class ShippingBlock extends BlockWithEntity {

	// Properties

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_block");

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

	// Init

	public ShippingBlock(Settings settings) {
		super(settings);
	}

	// Block

	@Override
	public BlockEntity createBlockEntity(BlockPos position, BlockState blockState) {
		return new ShippingBlockEntity(position, blockState);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		var blockPos = context.getBlockPos();
		var world = context.getWorld();

		if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(context)) {
			return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite()).with(HALF, DoubleBlockHalf.LOWER);
		} else {
			return null;
		}
	}

	@Override
	public void onPlaced(World world, BlockPos position, BlockState state, LivingEntity placer, ItemStack itemStack) {
		world.setBlockState(position.up(), state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
		super.onPlaced(world, position, state, placer, itemStack);
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(FACING);
		builder.add(HALF);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos position, BlockState newState, boolean moved) {
		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			return;
		}

		if (state.getBlock() == newState.getBlock()) {
			return;
		}

		var blockEntity = (ShippingBlockEntity) world.getBlockEntity(position);
		ItemScatterer.spawn(world, position, blockEntity);
	}

	public long getRenderingSeed(BlockState state, BlockPos position) {
		var x = position.getX();
		var y = position.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY();
		var z = position.getZ();

		return new BlockPos(x, y, z).hashCode();
	}

	// Ticker

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, ModBlockEntities.SHIPPING_BLOCK_ENTITY, ShippingBlockEntity::tick);
	}

	// Interaction

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos position, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient() || hand == Hand.OFF_HAND) {
			return ActionResult.CONSUME;
		}

		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			// Interaction with upper half is deferred to lower half instead.
			var lowerHalfBlockState = world.getBlockState(position.down());
			return lowerHalfBlockState.onUse(world, player, hand, hit);
		}

		var screenHandlerFactory = state.createScreenHandlerFactory(world, position);

		if (screenHandlerFactory == null) {
			return ActionResult.CONSUME;
		}

		player.openHandledScreen(screenHandlerFactory);
		return ActionResult.SUCCESS;
	}

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}

	// Utility

	private BlockPos getTwinBlockPosition(BlockPos position, BlockState state) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			return position.up();
		} else {
			return position.down();
		}
	}

}
