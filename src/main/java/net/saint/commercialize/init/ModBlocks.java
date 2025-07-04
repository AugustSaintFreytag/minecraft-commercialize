package net.saint.commercialize.init;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.saint.commercialize.block.MarketBlock;
import net.saint.commercialize.blockentity.MarketBlockEntity;

public final class ModBlocks {

	// Block Entities

	public static BlockEntityType<MarketBlockEntity> MARKET_BLOCK_ENTITY;

	// Blocks

	public static Block MARKET_BLOCK;

	// Init

	public static void initialize() {
		MARKET_BLOCK = registerBlockAndItem(MarketBlock.ID,
				new MarketBlock(AbstractBlock.Settings.create().nonOpaque().sounds(BlockSoundGroup.DEEPSLATE)));

		MARKET_BLOCK_ENTITY = registerBlockEntity(MarketBlockEntity.ID, MARKET_BLOCK, MarketBlockEntity::new);
	}

	// Registration

	private static Block registerBlockAndItem(Identifier id, Block block) {
		Registry.register(Registries.BLOCK, id, block);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));

		return block;
	}

	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(Identifier id, Block block,
			BlockEntityType.BlockEntityFactory<T> blockEntityConstructor) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, id,
				BlockEntityType.Builder.create(blockEntityConstructor, block).build(null));
	}
}
