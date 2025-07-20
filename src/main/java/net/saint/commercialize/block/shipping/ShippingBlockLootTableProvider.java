package net.saint.commercialize.block.shipping;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.init.ModBlocks;

public final class ShippingBlockLootTableProvider extends FabricBlockLootTableProvider {

	public ShippingBlockLootTableProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generate() {
		Commercialize.LOGGER.info("Registering loot table for shipping block.");
		this.addDrop(ModBlocks.SHIPPING_BLOCK, block -> drops(block));
	}

}
