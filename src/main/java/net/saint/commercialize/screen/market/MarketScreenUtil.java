package net.saint.commercialize.screen.market;

import net.minecraft.text.Text;
import net.saint.commercialize.library.OfferFilterMode;
import net.saint.commercialize.library.OfferSortMode;
import net.saint.commercialize.library.OfferSortOrder;
import net.saint.commercialize.library.PaymentMethod;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.util.LocalizationUtil;

public final class MarketScreenUtil {

	// Sort Mode

	public static TextureReference textureForSortMode(OfferSortMode sortMode) {
		return switch (sortMode) {
		case ITEM_NAME -> MarketScreenAssets.SORT_BY_NAME_ICON;
		case TIME_POSTED -> MarketScreenAssets.SORT_BY_TIME_ICON;
		case PRICE -> MarketScreenAssets.SORT_BY_PRICE_ICON;
		case PLAYER_NAME -> MarketScreenAssets.SORT_BY_PLAYER_ICON;
		};
	}

	public static Text tooltipTextForSortMode(OfferSortMode sortMode, OfferSortOrder sortOrder) {
		var sortModeName = sortMode.description().toLowerCase();
		var sortOrderName = sortOrder.description().toLowerCase();
		var sortModeDescription = LocalizationUtil.localizedString("gui", "market.sort_mode.description", sortModeName, sortOrderName);
		var description = sortModeDescription + ". " + LocalizationUtil.localizedString("gui", "market.sort_mode.tooltip");

		return Text.of(description);
	}

	// Filter Mode

	public static TextureReference textureForFilterMode(OfferFilterMode filterMode) {
		return switch (filterMode) {
		case AFFORDABLE -> MarketScreenAssets.FILTER_BY_PRICE_ICON;
		case ALL -> MarketScreenAssets.FILTER_BY_ALL_ICON;
		};
	}

	public static Text tooltipTextForFilterMode(OfferFilterMode filterMode) {
		var filterModeName = filterMode.description().toLowerCase();
		var filterModeDescription = LocalizationUtil.localizedString("gui", "market.filter_mode.description", filterModeName);
		var description = filterModeDescription + ". " + LocalizationUtil.localizedString("gui", "market.filter_mode.tooltip");

		return Text.of(description);
	}

	// Sort Order

	public static TextureReference textureForSortOrder(OfferSortOrder sortOrder) {
		return switch (sortOrder) {
		case ASCENDING -> MarketScreenAssets.SORT_ASCENDING_ICON;
		case DESCENDING -> MarketScreenAssets.SORT_DESCENDING_ICON;
		};
	}

	// Payment Method

	public static TextureReference textureForPaymentMethod(PaymentMethod paymentMethod) {
		return switch (paymentMethod) {
		case INVENTORY -> MarketScreenAssets.WALLET_ICON;
		case ACCOUNT -> MarketScreenAssets.CARD_ICON;
		};
	}

	public static Text labelTextForBalance(PaymentMethod paymentMethod) {
		return switch (paymentMethod) {
		case INVENTORY -> LocalizationUtil.localizedText("gui", "market.cash");
		case ACCOUNT -> LocalizationUtil.localizedText("gui", "market.bank");
		default -> Text.empty();
		};
	}

	public static Text tooltipTextForBalance(PaymentMethod paymentMethod) {
		return switch (paymentMethod) {
		case INVENTORY -> LocalizationUtil.localizedText("gui", "market.cash.tooltip");
		case ACCOUNT -> LocalizationUtil.localizedText("gui", "market.bank.tooltip");
		default -> Text.empty();
		};
	}

	public static Text tooltipTextForPaymentMethod(PaymentMethod paymentMethod) {
		switch (paymentMethod) {
		case INVENTORY:
			return LocalizationUtil.localizedText("gui", "market.payment_mode.inventory.tooltip");
		case ACCOUNT:
			return LocalizationUtil.localizedText("gui", "market.payment_mode.account.tooltip");
		default:
			return Text.empty();
		}
	}

}
