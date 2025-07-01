package net.saint.commercialize.screen.market;

import net.minecraft.text.Text;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.gui.assets.MarketAssets;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.util.LocalizationUtil;

public final class MarketScreenUtil {

	// Sort Mode

	public static TextureReference textureForSortMode(OfferSortMode sortMode) {
		return switch (sortMode) {
		case ITEM_NAME -> MarketAssets.SORT_BY_NAME_ICON;
		case TIME_POSTED -> MarketAssets.SORT_BY_TIME_ICON;
		case PRICE -> MarketAssets.SORT_BY_PRICE_ICON;
		case PLAYER_NAME -> MarketAssets.SORT_BY_PLAYER_ICON;
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
		case AFFORDABLE -> MarketAssets.FILTER_BY_PRICE_ICON;
		case ALL -> MarketAssets.FILTER_BY_ALL_ICON;
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
		case ASCENDING -> MarketAssets.SORT_ASCENDING_ICON;
		case DESCENDING -> MarketAssets.SORT_DESCENDING_ICON;
		};
	}

	// Payment Method

	public static TextureReference textureForPaymentMethod(PaymentMethod paymentMethod) {
		return switch (paymentMethod) {
		case INVENTORY -> MarketAssets.WALLET_ICON;
		case ACCOUNT -> MarketAssets.CARD_ICON;
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
