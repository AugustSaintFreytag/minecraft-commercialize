package net.saint.commercialize.init;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.saint.commercialize.block.market.MarketBlock;
import net.saint.commercialize.block.shipping.ShippingBlock;

public final class ModBlocks {

	// Blocks

	public static Block MARKET_BLOCK;
	public static Block SHIPPING_BLOCK;

	// Init

	public static void initialize() {
		MARKET_BLOCK = registerBlockAndItem(MarketBlock.ID,
				new MarketBlock(FabricBlockSettings.create().strength(1.0f, 3600000.0f).nonOpaque().sounds(BlockSoundGroup.METAL)));
		BlockRenderLayerMap.INSTANCE.putBlock(MARKET_BLOCK, RenderLayer.getCutout());

		SHIPPING_BLOCK = registerBlockAndItem(ShippingBlock.ID,
				new ShippingBlock(FabricBlockSettings.create().strength(1.0f, 3600000.0f).nonOpaque().sounds(BlockSoundGroup.METAL)));
		BlockRenderLayerMap.INSTANCE.putBlock(SHIPPING_BLOCK, RenderLayer.getCutout());
	}

	// Registration

	private static Block registerBlockAndItem(Identifier id, Block block) {
		Registry.register(Registries.BLOCK, id, block);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));

		return block;
	}

}
