package net.saint.commercialize.init;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.saint.commercialize.block.market.MarketBlockEntity;
import net.saint.commercialize.block.shipping.ShippingBlockEntity;

public final class ModBlockEntities {

	// Block Entities

	public static BlockEntityType<MarketBlockEntity> MARKET_BLOCK_ENTITY;
	public static BlockEntityType<ShippingBlockEntity> SHIPPING_BLOCK_ENTITY;

	// Init

	public static void initialize() {

		MARKET_BLOCK_ENTITY = registerBlockEntity(MarketBlockEntity.ID, ModBlocks.MARKET_BLOCK, MarketBlockEntity::new);

		SHIPPING_BLOCK_ENTITY = registerBlockEntity(ShippingBlockEntity.ID, ModBlocks.SHIPPING_BLOCK, ShippingBlockEntity::new);
	}

	// Registration

	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(Identifier id, Block block,
			BlockEntityType.BlockEntityFactory<T> blockEntityConstructor) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, id,
				BlockEntityType.Builder.create(blockEntityConstructor, block).build(null));
	}

}
