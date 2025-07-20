package net.saint.commercialize.block.market;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.init.ModBlocks;

public final class MarketBlockLootTableProvider extends FabricBlockLootTableProvider {

	public MarketBlockLootTableProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generate() {
		Commercialize.LOGGER.info("Registering loot table for market block.");
		this.addDrop(ModBlocks.MARKET_BLOCK, block -> drops(block));
	}

}
