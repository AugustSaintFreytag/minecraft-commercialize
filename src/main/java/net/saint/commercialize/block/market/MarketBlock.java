package net.saint.commercialize.block.market;

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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
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

public class MarketBlock extends DoubleBlockWithEntity {

	// Properties

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_block");

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	private static final HashMap<DoubleBlockHalf, VoxelShape> SHAPES = new HashMap<>() {
		{
			this.put(DoubleBlockHalf.LOWER,
					VoxelShapes.union(VoxelShapes.cuboid(0.1875, 0.0625, 0.1875, 0.8125, 0.125, 0.8125),
							VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.0625, 0.9375),
							VoxelShapes.cuboid(0.3125, 0.0625, 0.3125, 0.6875, 0.75, 0.6875),
							VoxelShapes.cuboid(0.0625, 0.75, 0.0625, 0.9375, 1, 0.9375)));
			this.put(DoubleBlockHalf.UPPER,
					VoxelShapes.union(VoxelShapes.cuboid(0.875, 0.8125, 0.0625, 0.9375, 0.9375, 0.3125),
							VoxelShapes.cuboid(0.875, 0.6875, 0.125, 0.9375, 0.8125, 0.375),
							VoxelShapes.cuboid(0.875, 0.5625, 0.1875, 0.9375, 0.6875, 0.3125),
							VoxelShapes.cuboid(0.875, 0.4375, 0.25, 0.9375, 0.5625, 0.3125),
							VoxelShapes.cuboid(0.0625, 0.8125, 0.0625, 0.125, 0.9375, 0.3125),
							VoxelShapes.cuboid(0.0625, 0.6875, 0.125, 0.125, 0.8125, 0.3125),
							VoxelShapes.cuboid(0.0625, 0.5625, 0.1875, 0.125, 0.6875, 0.3125),
							VoxelShapes.cuboid(0.0625, 0.4375, 0.25, 0.125, 0.5625, 0.3125),
							VoxelShapes.cuboid(0.125, 0.875, 0.3125, 0.875, 0.9375, 0.375),
							VoxelShapes.cuboid(0.125, 0.1875, 0.3125, 0.875, 0.3125, 0.375),
							VoxelShapes.cuboid(0.0625, 0.1875, 0.3125, 0.125, 0.9375, 0.375),
							VoxelShapes.cuboid(0.875, 0.1875, 0.3125, 0.9375, 0.9375, 0.375),
							VoxelShapes.cuboid(0.125, 0.1875, 0.125, 0.875, 0.25, 0.3125),
							VoxelShapes.cuboid(0.0625, 0.9375, 0.0625, 0.9375, 1, 0.9375),
							VoxelShapes.cuboid(0.0625, 0.1875, 0.375, 0.9375, 0.9375, 0.9375),
							VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.1875, 0.9375)));
		}
	};

	private static final HashMap<DoubleBlockHalf, HashMap<Direction, VoxelShape>> SHAPES_BY_DIRECTION = new HashMap<>() {
		{
			this.put(DoubleBlockHalf.LOWER, VoxelShapeUtil.createDirectionalShapes(SHAPES.get(DoubleBlockHalf.LOWER)));
			this.put(DoubleBlockHalf.UPPER, VoxelShapeUtil.createDirectionalShapes(SHAPES.get(DoubleBlockHalf.UPPER)));
		}
	};

	// Init

	public MarketBlock(Settings settings) {
		super(settings);
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

	// Entity

	@Override
	public BlockEntity createBlockEntity(BlockPos position, BlockState blockState) {
		if (blockState.get(HALF) == DoubleBlockHalf.UPPER) {
			return null;
		}

		return new MarketBlockEntity(position, blockState);
	}

	// Placement

	@Override
	public BlockState getMasterPlacementState(ItemPlacementContext context) {
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

	// Mutation

	@Override
	protected void onStateReplacedLowerHalf(BlockState state, World world, BlockPos position, BlockState newState, boolean moved) {
		// No special action needed.
	}

	// Interaction

	@Override
	protected ActionResult onMasterBlockUse(BlockState state, World world, BlockPos position, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		if (hand == Hand.OFF_HAND) {
			return ActionResult.FAIL;
		}

		if (!world.isClient()) {
			return ActionResult.SUCCESS;
		}

		var blockEntity = (MarketBlockEntity) world.getBlockEntity(position);
		blockEntity.openMarketScreen(world, player);

		player.swingHand(hand);

		return ActionResult.SUCCESS;
	}

	// Ticking

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, ModBlockEntities.MARKET_BLOCK_ENTITY, MarketBlockEntity::tick);
	}

	// Redstone

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}

}
