package net.saint.commercialize.block.market;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
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
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.DoubleBlockWithEntity;
import net.saint.commercialize.init.ModBlockEntities;

public class MarketBlock extends DoubleBlockWithEntity {

	// Properties

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_block");

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	// Init

	public MarketBlock(Settings settings) {
		super(settings);
	}

	// Entity

	@Override
	public BlockEntity createBlockEntity(BlockPos position, BlockState blockState) {
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
	public ActionResult onUse(BlockState state, World world, BlockPos position, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient() || hand == Hand.OFF_HAND) {
			return ActionResult.PASS;
		}

		var blockEntity = (MarketBlockEntity) world.getBlockEntity(position);
		blockEntity.openMarketScreen(world, player);

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
