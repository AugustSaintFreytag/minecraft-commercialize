package net.saint.commercialize.init;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.saint.commercialize.screen.shipping.ShippingBlockScreen;

public final class ModScreens {

	// Init

	public static void initialize() {
		HandledScreens.register(ModScreenHandlers.SHIPPING_BLOCK_SCREEN_HANDLER, ShippingBlockScreen::new);
	}

}
