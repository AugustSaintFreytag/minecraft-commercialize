package net.saint.commercialize.block.posting;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.DoubleBlockWithEntity;
import net.saint.commercialize.init.ModBlockEntities;
import net.saint.commercialize.util.VoxelShapeUtil;

public class PostingBlock extends DoubleBlockWithEntity {

	// Properties

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "posting_block");

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	private static final HashMap<DoubleBlockHalf, VoxelShape> SHAPES = new HashMap<>() {
		{
			this.put(DoubleBlockHalf.LOWER, VoxelShapes.cuboid(0, 0, 0.0625, 1, 1, 0.9375));
			this.put(DoubleBlockHalf.UPPER, VoxelShapes.cuboid(0, 0, 0.0625, 1, 0.8125, 0.9375));
		}
	};

	private static final HashMap<DoubleBlockHalf, HashMap<Direction, VoxelShape>> SHAPES_BY_DIRECTION = new HashMap<>() {
		{
			this.put(DoubleBlockHalf.LOWER, VoxelShapeUtil.createDirectionalShapes(SHAPES.get(DoubleBlockHalf.LOWER)));
			this.put(DoubleBlockHalf.UPPER, VoxelShapeUtil.createDirectionalShapes(SHAPES.get(DoubleBlockHalf.UPPER)));
		}
	};

	// Init

	public PostingBlock(Settings settings) {
		super(settings);
	}

	// Entity

	@Override
	public BlockEntity createBlockEntity(BlockPos position, BlockState blockState) {
		if (blockState.get(HALF) == DoubleBlockHalf.UPPER) {
			return null;
		}

		return new PostingBlockEntity(position, blockState);
	}

	// Placement

	@Override
	protected BlockState getMasterPlacementState(ItemPlacementContext context) {
		return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	// Shape

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPES_BY_DIRECTION.get(state.get(HALF)).get(state.get(FACING));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPES_BY_DIRECTION.get(state.get(HALF)).get(state.get(FACING));
	}

	// Mutation

	@Override
	protected void onStateReplacedLowerHalf(BlockState state, World world, BlockPos position, BlockState newState, boolean moved) {
		var blockEntity = world.getBlockEntity(position, ModBlockEntities.POSTING_BLOCK_ENTITY);

		if (!blockEntity.isPresent()) {
			Commercialize.LOGGER.error("Could not acquire block entity for posting block at position {}.", position);
			return;
		}

		ItemScatterer.spawn(world, position, blockEntity.get());
	}

	// Interaction

	@Override
	protected ActionResult onMasterBlockUse(BlockState state, World world, BlockPos position, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		if (hand == Hand.OFF_HAND) {
			return ActionResult.FAIL;
		}

		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}

		var screenHandlerFactory = state.createScreenHandlerFactory(world, position);

		if (screenHandlerFactory == null) {
			return ActionResult.FAIL;
		}

		player.openHandledScreen(screenHandlerFactory);
		return ActionResult.SUCCESS;
	}

	// Ticker

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, ModBlockEntities.POSTING_BLOCK_ENTITY, PostingBlockEntity::tick);
	}

}
