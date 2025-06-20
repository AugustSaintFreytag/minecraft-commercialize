package net.saint.commercialize.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.init.Blocks;

public class MarketBlockEntity extends BlockEntity {

	// Properties

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_block_entity");

	// Init

	public MarketBlockEntity(BlockPos pos, BlockState state) {
		super(Blocks.MARKET_BLOCK_ENTITY, pos, state);
	}

}
