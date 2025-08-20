package net.saint.commercialize.screen.icons;

import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.library.TextureReference;

public final class ScreenAssets {

	// Configuration

	public static final Identifier ICON_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/all_icons.png");

	// Stubs

	public static final TextureReference STUB = new TextureReference(ICON_TEXTURE, 0, 0, 1, 1);

	// Buttons

	public static final TextureReference SORT_BY_NAME_ICON = new TextureReference(ICON_TEXTURE, 64, 16, 16, 16);
	public static final TextureReference SORT_BY_TIME_ICON = new TextureReference(ICON_TEXTURE, 48, 16, 16, 16);
	public static final TextureReference SORT_BY_PRICE_ICON = new TextureReference(ICON_TEXTURE, 32, 16, 16, 16);
	public static final TextureReference SORT_BY_PLAYER_ICON = new TextureReference(ICON_TEXTURE, 16, 16, 16, 16);
	public static final TextureReference FILTER_BY_PRICE_ICON = new TextureReference(ICON_TEXTURE, 16, 32, 16, 16);
	public static final TextureReference FILTER_BY_ALL_ICON = new TextureReference(ICON_TEXTURE, 32, 32, 16, 16);

	public static final TextureReference EMPTY_CART_ICON = new TextureReference(ICON_TEXTURE, 96, 16, 16, 16);
	public static final TextureReference CONFIRM_ORDER_ICON = new TextureReference(ICON_TEXTURE, 112, 16, 16, 16);
	public static final TextureReference RESET_PRICE_ICON = new TextureReference(ICON_TEXTURE, 128, 16, 16, 16);
	public static final TextureReference WALLET_ICON = new TextureReference(ICON_TEXTURE, 96, 32, 16, 16);
	public static final TextureReference CARD_ICON = new TextureReference(ICON_TEXTURE, 112, 32, 16, 16);
	public static final TextureReference SPECIFIED_CARD_ICON = new TextureReference(ICON_TEXTURE, 128, 32, 16, 16);

	// Decoratives

	public static final TextureReference SORT_DESCENDING_ICON = new TextureReference(ICON_TEXTURE, 16, 0, 16, 16);
	public static final TextureReference SORT_ASCENDING_ICON = new TextureReference(ICON_TEXTURE, 32, 0, 16, 16);

	public static final TextureReference CHEVRON_ICON = new TextureReference(ICON_TEXTURE, 100, 8, 11, 7);
	public static final TextureReference CHEVRON_SMALL_ICON = new TextureReference(ICON_TEXTURE, 120, 10, 7, 5);

}
