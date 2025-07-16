package net.saint.commercialize.screen.market;

import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.library.TextureReference;

public final class MarketScreenAssets {

	// Configuration

	public static final Identifier LEFT_PANEL_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_left_panel.png");
	public static final Identifier RIGHT_PANEL_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_right_panel.png");
	public static final Identifier LIST_ITEMS_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_list_items.png");
	public static final Identifier ICON_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_icons.png");
	public static final Identifier PLAYER_HEADS = new Identifier(Commercialize.MOD_ID, "textures/gui/player_heads.png");

	// Stubs

	public static final TextureReference STUB_ICON = new TextureReference(ICON_TEXTURE, 0, 0, 1, 1);

	// Screen Panels

	public static final TextureReference LEFT_PANEL = new TextureReference(LEFT_PANEL_TEXTURE, 0, 0, 214, 192);
	public static final TextureReference RIGHT_PANEL = new TextureReference(RIGHT_PANEL_TEXTURE, 0, 0, 192, 178);

	// Button Icons

	public static final TextureReference SORT_BY_NAME_ICON = new TextureReference(ICON_TEXTURE, 64, 16, 16, 16);
	public static final TextureReference SORT_BY_TIME_ICON = new TextureReference(ICON_TEXTURE, 48, 16, 16, 16);
	public static final TextureReference SORT_BY_PRICE_ICON = new TextureReference(ICON_TEXTURE, 32, 16, 16, 16);
	public static final TextureReference SORT_BY_PLAYER_ICON = new TextureReference(ICON_TEXTURE, 16, 16, 16, 16);
	public static final TextureReference FILTER_BY_PRICE_ICON = new TextureReference(ICON_TEXTURE, 16, 32, 16, 16);
	public static final TextureReference FILTER_BY_ALL_ICON = new TextureReference(ICON_TEXTURE, 32, 32, 16, 16);

	public static final TextureReference EMPTY_CART_ICON = new TextureReference(ICON_TEXTURE, 96, 16, 16, 16);
	public static final TextureReference CONFIRM_ORDER_ICON = new TextureReference(ICON_TEXTURE, 112, 16, 16, 16);
	public static final TextureReference WALLET_ICON = new TextureReference(ICON_TEXTURE, 96, 32, 16, 16);
	public static final TextureReference CARD_ICON = new TextureReference(ICON_TEXTURE, 112, 32, 16, 16);

	// Icon Decoratives

	public static final TextureReference SORT_DESCENDING_ICON = new TextureReference(ICON_TEXTURE, 16, 0, 16, 16);
	public static final TextureReference SORT_ASCENDING_ICON = new TextureReference(ICON_TEXTURE, 32, 0, 16, 16);

	// List Items

	public static final TextureReference OFFER_LIST_ITEM = new TextureReference(LIST_ITEMS_TEXTURE, 0, 0, 167, 18);
	public static final TextureReference OFFER_LIST_ITEM_HIGHLIGHT = new TextureReference(LIST_ITEMS_TEXTURE, 0, 18, 167, 18);
	public static final TextureReference OFFER_DISABLED_LIST_ITEM = new TextureReference(LIST_ITEMS_TEXTURE, 0, 36, 167, 18);
	public static final TextureReference OFFER_CAP_LIST_ITEM = new TextureReference(LIST_ITEMS_TEXTURE, 0, 54, 167, 18);

	public static final TextureReference CART_LIST_ITEM = new TextureReference(LIST_ITEMS_TEXTURE, 0, 98, 133, 18);
	public static final TextureReference CART_LIST_ITEM_HIGHLIGHT = new TextureReference(LIST_ITEMS_TEXTURE, 0, 116, 133, 18);

	// Indicator Graphics

	public static final TextureReference EMPTY_CART_INDICATOR = new TextureReference(ICON_TEXTURE, 16, 64, 48, 43);

}
