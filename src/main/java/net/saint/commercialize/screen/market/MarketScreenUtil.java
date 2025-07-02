package net.saint.commercialize.screen.market;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.gui.assets.MarketAssets;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.util.LocalizationUtil;
import net.saint.commercialize.util.NumericFormattingUtil;
import net.saint.commercialize.util.TextFormattingUtil;
import net.saint.commercialize.util.TimeFormattingUtil;

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

	// Offer

	public static List<TooltipComponent> tooltipTextForOffer(World world, Offer offer) {
		var components = new ArrayList<TooltipComponent>();

		// Heading

		var headingText = LocalizationUtil.localizedText("text", "offer").copy().formatted(Formatting.BOLD, Formatting.YELLOW);
		components.add(TooltipComponent.of(headingText.asOrderedText()));

		// Item Name

		var nameText = offer.stack.getName().copy();
		nameText.setStyle(Style.EMPTY.withColor(Formatting.WHITE));

		if (offer.stack.hasCustomName()) {
			nameText.setStyle(nameText.getStyle().withItalic(true));
		}

		if (offer.stack.getCount() > 1) {
			var countText = Text.of(" (x" + offer.stack.getCount() + ")").copy();
			countText.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
			nameText.append(countText);
		}

		components.add(TooltipComponent.of(nameText.asOrderedText()));

		// Price

		var priceText = LocalizationUtil.localizedText("text", "offer.price", NumericFormattingUtil.formatCurrency(offer.price));
		components.add(TooltipComponent.of(priceText.asOrderedText()));

		// Seller

		var sellerText = LocalizationUtil.localizedText("text", "offer.seller", offer.sellerName);
		components.add(TooltipComponent.of(sellerText.asOrderedText()));

		// Time

		var currentTicks = world.getTime();
		var elapsedTicks = currentTicks - offer.timestamp;
		var timeExpiresTicks = Math.max(0, (offer.timestamp + offer.duration) - currentTicks);

		var formattedTimePosted = TextFormattingUtil.capitalize(TimeFormattingUtil.formattedTime(elapsedTicks));
		var timePostedText = LocalizationUtil.localizedText("text", "offer.time_posted", formattedTimePosted);
		components.add(TooltipComponent.of(timePostedText.asOrderedText()));

		var formattedTimeExpires = TextFormattingUtil.capitalize(TimeFormattingUtil.formattedTime(timeExpiresTicks));
		var expiryText = LocalizationUtil.localizedText("text", "offer.time_expiring", formattedTimeExpires);
		components.add(TooltipComponent.of(expiryText.asOrderedText()));

		return components;
	}

	// Seller

	public static List<TooltipComponent> tooltipTextForSeller(Offer offer) {
		var components = new ArrayList<TooltipComponent>();

		// Seller Name

		var sellerNameText = Text.of(offer.sellerName).copy();
		// sellerNameText.setStyle(sellerNameText.getStyle());
		components.add(TooltipComponent.of(sellerNameText.asOrderedText()));

		if (offer.isGenerated) {
			var generatedText = LocalizationUtil.localizedText("text", "offer.generated").copy();
			generatedText.setStyle(generatedText.getStyle().withItalic(true).withColor(Formatting.GRAY));
			components.add(TooltipComponent.of(generatedText.asOrderedText()));
		}

		return components;
	}

}
