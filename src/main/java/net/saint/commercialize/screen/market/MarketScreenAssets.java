package net.saint.commercialize.screen.market;

import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.library.TextureReference;

public final class MarketScreenAssets {

	// Configuration

	public static final Identifier LEFT_PANEL_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_left_panel.png");
	public static final Identifier RIGHT_PANEL_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_right_panel.png");
	public static final Identifier LIST_ITEMS_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_list_items.png");

	public static final Identifier PLAYER_HEADS_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/player_heads.png");

	// Screen Panels

	public static final TextureReference LEFT_PANEL = new TextureReference(LEFT_PANEL_TEXTURE, 0, 0, 214, 192);
	public static final TextureReference RIGHT_PANEL = new TextureReference(RIGHT_PANEL_TEXTURE, 0, 0, 192, 178);

	// List Items

	public static final TextureReference OFFER_LIST_ITEM = new TextureReference(LIST_ITEMS_TEXTURE, 0, 0, 167, 18);
	public static final TextureReference OFFER_LIST_ITEM_HIGHLIGHT = new TextureReference(LIST_ITEMS_TEXTURE, 0, 18, 167, 18);
	public static final TextureReference OFFER_DISABLED_LIST_ITEM = new TextureReference(LIST_ITEMS_TEXTURE, 0, 36, 167, 18);
	public static final TextureReference OFFER_CAP_LIST_ITEM = new TextureReference(LIST_ITEMS_TEXTURE, 0, 54, 167, 18);

	public static final TextureReference CART_LIST_ITEM = new TextureReference(LIST_ITEMS_TEXTURE, 0, 98, 133, 18);
	public static final TextureReference CART_LIST_ITEM_HIGHLIGHT = new TextureReference(LIST_ITEMS_TEXTURE, 0, 116, 133, 18);

	// Indicator Graphics

	public static final TextureReference POSITIVE_BALANCE_BACKDROP = new TextureReference(RIGHT_PANEL_TEXTURE, 45, 182, 109, 13);
	public static final TextureReference NEGATIVE_BALANCE_BACKDROP = new TextureReference(RIGHT_PANEL_TEXTURE, 45, 197, 109, 13);

	public static final TextureReference EMPTY_CART_INDICATOR = new TextureReference(RIGHT_PANEL_TEXTURE, 168, 182, 48, 43);

}
