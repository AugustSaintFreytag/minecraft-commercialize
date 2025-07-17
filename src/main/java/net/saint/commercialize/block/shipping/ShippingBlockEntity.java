package net.saint.commercialize.block.shipping;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.init.ModBlocks;

public class ShippingBlockEntity extends BlockEntity {

	// Properties

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_block_entity");

	// Init

	public ShippingBlockEntity(BlockPos position, BlockState state) {
		super(ModBlocks.SHIPPING_BLOCK_ENTITY, position, state);
	}

	// Tick

	public static void tick(World world, BlockPos position, BlockState state, ShippingBlockEntity blockEntity) {
	}

}
