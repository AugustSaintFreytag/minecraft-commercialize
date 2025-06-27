package net.saint.commercialize.library;

import net.saint.commercialize.util.LocalizationUtil;

public enum OfferSortMode {

	ITEM_NAME, TIME_POSTED, PRICE, PLAYER_NAME;

	public OfferSortMode next() {
		var modes = values();
		var nextIndex = (this.ordinal() + 1) % modes.length;

		return modes[nextIndex];
	}

	public String description() {
		return switch (this) {
		case ITEM_NAME -> LocalizationUtil.localizedString("gui", "market.sort_mode.item_name");
		case TIME_POSTED -> LocalizationUtil.localizedString("gui", "market.sort_mode.time_posted");
		case PRICE -> LocalizationUtil.localizedString("gui", "market.sort_mode.price");
		case PLAYER_NAME -> LocalizationUtil.localizedString("gui", "market.sort_mode.player_name");
		};
	}

}
