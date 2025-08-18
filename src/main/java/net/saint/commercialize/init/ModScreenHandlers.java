package net.saint.commercialize.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.saint.commercialize.screen.posting.PostingScreenHandler;
import net.saint.commercialize.screen.shipping.ShippingScreenHandler;

public final class ModScreenHandlers {

	// Screen Handlers

	public static ScreenHandlerType<ShippingScreenHandler> SHIPPING_SCREEN_HANDLER;
	public static ScreenHandlerType<PostingScreenHandler> POSTING_SCREEN_HANDLER;

	// Init

	public static void initialize() {
		SHIPPING_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, ShippingScreenHandler.ID,
				new ScreenHandlerType<>(ShippingScreenHandler::new, FeatureSet.of(FeatureFlags.VANILLA)));
		POSTING_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, PostingScreenHandler.ID,
				new ScreenHandlerType<>(PostingScreenHandler::new, FeatureSet.of(FeatureFlags.VANILLA)));
	}

}
