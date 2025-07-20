package net.saint.commercialize;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.saint.commercialize.block.market.MarketBlockLootTableProvider;
import net.saint.commercialize.block.shipping.ShippingBlockLootTableProvider;

public final class CommercializeDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		var pack = generator.createPack();

		Commercialize.LOGGER.info("Loading data pack generation.");
		pack.addProvider(MarketBlockLootTableProvider::new);
		pack.addProvider(ShippingBlockLootTableProvider::new);
	}

}
