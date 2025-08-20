package net.saint.commercialize.data.offer;

import net.saint.commercialize.util.LocalizationUtil;

public enum OfferFilterMode {
	ALL, AFFORDABLE;

	public OfferFilterMode next() {
		var modes = values();
		var nextIndex = (this.ordinal() + 1) % modes.length;

		return modes[nextIndex];
	}

	public String description() {
		return switch (this) {
		case ALL -> LocalizationUtil.localizedString("gui", "market.filter_mode.all");
		case AFFORDABLE -> LocalizationUtil.localizedString("gui", "market.filter_mode.affordable");
		};
	}
}
