package net.saint.commercialize.screen.shipping;

import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.library.TextureReference;

public final class ShippingScreenAssets {

	// Configuration

	public static final Identifier TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/shipping.png");

	// Stubs

	public static final TextureReference STUB_ICON = new TextureReference(TEXTURE, 255, 255, 1, 1);

	// Screen Panels

	public static final TextureReference PANEL = new TextureReference(TEXTURE, 0, 0, 210, 177);

}
