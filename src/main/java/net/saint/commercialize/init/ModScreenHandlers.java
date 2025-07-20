package net.saint.commercialize.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.saint.commercialize.screen.shipping.ShippingBlockScreenHandler;

public final class ModScreenHandlers {

	// Screen Handlers

	public static ScreenHandlerType<ShippingBlockScreenHandler> SHIPPING_BLOCK_SCREEN_HANDLER;

	// Init

	public static void initialize() {
		SHIPPING_BLOCK_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, ShippingBlockScreenHandler.ID,
				new ScreenHandlerType<>(ShippingBlockScreenHandler::new, FeatureSet.of(FeatureFlags.VANILLA)));
	}

}
