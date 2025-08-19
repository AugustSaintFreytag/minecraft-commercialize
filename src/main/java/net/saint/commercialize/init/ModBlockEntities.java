package net.saint.commercialize.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.saint.commercialize.block.market.MarketBlockEntity;
import net.saint.commercialize.block.posting.PostingBlockEntity;
import net.saint.commercialize.block.shipping.ShippingBlockEntity;

public final class ModBlockEntities {

	// Block Entities

	public static BlockEntityType<MarketBlockEntity> MARKET_BLOCK_ENTITY;
	public static BlockEntityType<ShippingBlockEntity> SHIPPING_BLOCK_ENTITY;
	public static BlockEntityType<PostingBlockEntity> POSTING_BLOCK_ENTITY;

	// Init

	public static void initialize() {
		MARKET_BLOCK_ENTITY = registerBlockEntity(MarketBlockEntity.ID, ModBlocks.MARKET_BLOCK, MarketBlockEntity::new);
		SHIPPING_BLOCK_ENTITY = registerBlockEntity(ShippingBlockEntity.ID, ModBlocks.SHIPPING_BLOCK, ShippingBlockEntity::new);
		POSTING_BLOCK_ENTITY = registerBlockEntity(PostingBlockEntity.ID, ModBlocks.POSTING_BLOCK, PostingBlockEntity::new);
	}

	// Registration

	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(Identifier id, Block block,
			FabricBlockEntityTypeBuilder.Factory<T> blockEntityConstructor) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, id,
				FabricBlockEntityTypeBuilder.create(blockEntityConstructor, block).build(null));
	}

}
