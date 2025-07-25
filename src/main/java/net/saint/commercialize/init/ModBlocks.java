package net.saint.commercialize.init;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.saint.commercialize.block.market.MarketBlock;
import net.saint.commercialize.block.market.MarketBlockEntity;
import net.saint.commercialize.block.shipping.ShippingBlock;
import net.saint.commercialize.block.shipping.ShippingBlockEntity;

public final class ModBlocks {

	// Block Entities

	public static BlockEntityType<MarketBlockEntity> MARKET_BLOCK_ENTITY;
	public static BlockEntityType<ShippingBlockEntity> SHIPPING_BLOCK_ENTITY;

	// Blocks

	public static Block MARKET_BLOCK;
	public static Block SHIPPING_BLOCK;

	// Init

	public static void initialize() {
		MARKET_BLOCK = registerBlockAndItem(MarketBlock.ID,
				new MarketBlock(FabricBlockSettings.create().strength(1.0f, 3600000.0f).nonOpaque().sounds(BlockSoundGroup.METAL)));
		MARKET_BLOCK_ENTITY = registerBlockEntity(MarketBlockEntity.ID, MARKET_BLOCK, MarketBlockEntity::new);

		SHIPPING_BLOCK = registerBlockAndItem(ShippingBlock.ID,
				new ShippingBlock(FabricBlockSettings.create().strength(1.0f, 3600000.0f).nonOpaque().sounds(BlockSoundGroup.METAL)));
		SHIPPING_BLOCK_ENTITY = registerBlockEntity(ShippingBlockEntity.ID, SHIPPING_BLOCK, ShippingBlockEntity::new);
	}

	// Registration

	private static Block registerBlockAndItem(Identifier id, Block block) {
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
		Registry.register(Registries.BLOCK, id, block);

		return block;
	}

	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(Identifier id, Block block,
			BlockEntityType.BlockEntityFactory<T> blockEntityConstructor) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, id,
				BlockEntityType.Builder.create(blockEntityConstructor, block).build(null));
	}
}
