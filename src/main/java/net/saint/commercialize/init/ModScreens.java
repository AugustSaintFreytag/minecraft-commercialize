package net.saint.commercialize.init;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.saint.commercialize.screen.selling.SellingScreen;
import net.saint.commercialize.screen.shipping.ShippingScreen;

public final class ModScreens {

	// Init

	public static void initialize() {
		HandledScreens.register(ModScreenHandlers.SHIPPING_SCREEN_HANDLER, ShippingScreen::new);
		HandledScreens.register(ModScreenHandlers.SELLING_SCREEN_HANDLER, SellingScreen::new);
	}

}
