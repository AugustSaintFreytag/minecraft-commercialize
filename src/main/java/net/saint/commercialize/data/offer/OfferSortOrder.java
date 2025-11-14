package net.saint.commercialize.data.offer;

import net.saint.commercialize.util.localization.LocalizationUtil;

public enum OfferSortOrder {
	ASCENDING, DESCENDING;

	public OfferSortOrder next() {
		var orders = values();
		var nextIndex = (this.ordinal() + 1) % orders.length;

		return orders[nextIndex];
	}

	public String description() {
		return switch (this) {
			case ASCENDING -> LocalizationUtil.localizedString("gui", "market.sort_order.ascending");
			case DESCENDING -> LocalizationUtil.localizedString("gui", "market.sort_order.descending");
		};
	}
}
