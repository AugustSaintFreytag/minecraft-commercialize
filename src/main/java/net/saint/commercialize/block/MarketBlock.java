package net.saint.commercialize.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.blockentity.MarketBlockEntity;
import net.saint.commercialize.screen.market.MarketScreen;

public class MarketBlock extends BlockWithEntity {

	// Properties

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market");

	public static final DirectionProperty FACING = net.minecraft.state.property.Properties.HORIZONTAL_FACING;

	// Init

	public MarketBlock(Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos position, BlockState blockState) {
		return new MarketBlockEntity(position, blockState);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
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

	// Interaction

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient()) {
			return ActionResult.SUCCESS;
		}

		var client = MinecraftClient.getInstance();
		client.setScreen(new MarketScreen());

		return ActionResult.PASS;
	}

}
