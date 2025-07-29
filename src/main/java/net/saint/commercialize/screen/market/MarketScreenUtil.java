package net.saint.commercialize.screen.market;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;
import net.saint.commercialize.data.text.ItemDescriptionUtil;
import net.saint.commercialize.data.text.TextFormattingUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;
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
		case SPECIFIED_ACCOUNT -> MarketScreenAssets.SPECIFIED_CARD_ICON;
		};
	}

	public static Text labelTextForBalance(PaymentMethod paymentMethod) {
		return switch (paymentMethod) {
		case INVENTORY -> LocalizationUtil.localizedText("gui", "market.cash");
		case ACCOUNT -> LocalizationUtil.localizedText("gui", "market.account");
		case SPECIFIED_ACCOUNT -> LocalizationUtil.localizedText("gui", "market.account");
		default -> Text.of("...");
		};
	}

	public static Text tooltipTextForBalance(PaymentMethod paymentMethod, String ownerName) {
		return switch (paymentMethod) {
		case INVENTORY -> LocalizationUtil.localizedText("gui", "market.cash.tooltip");
		case ACCOUNT -> LocalizationUtil.localizedText("gui", "market.account.tooltip");
		case SPECIFIED_ACCOUNT -> LocalizationUtil.localizedText("gui", "market.specified_account.tooltip", ownerName);
		default -> Text.of("...");
		};
	}

	public static Text tooltipTextForPaymentMethod(PaymentMethod paymentMethod, String ownerName) {
		switch (paymentMethod) {
		case INVENTORY:
			return LocalizationUtil.localizedText("gui", "market.payment_mode.inventory.tooltip");
		case ACCOUNT:
			return LocalizationUtil.localizedText("gui", "market.payment_mode.account.tooltip");
		case SPECIFIED_ACCOUNT:
			return LocalizationUtil.localizedText("gui", "market.payment_mode.specified_account.tooltip", ownerName);
		default:
			return Text.empty();
		}
	}

	// Offer

	public static List<TooltipComponent> tooltipTextForOffer(World world, Offer offer) {
		var components = new ArrayList<TooltipComponent>();

		// Title
		var title = LocalizationUtil.localizedText("text", "offer.tooltip.title").copy();
		title.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xf1c513)).withBold(true));
		components.add(TooltipComponent.of(title.asOrderedText()));

		// Item Name
		var nameLabel = Text.literal(LocalizationUtil.localizedString("text", "offer.tooltip.item") + ": ")
				.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
		var nameText = offer.stack.getName().copy().setStyle(Style.EMPTY.withColor(Formatting.WHITE));

		if (offer.stack.hasCustomName()) {
			nameText.setStyle(nameText.getStyle().withItalic(true));
		}

		if (offer.stack.getCount() > 1) {
			var countText = Text.literal(" (x" + offer.stack.getCount() + ")").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
			nameText.append(countText);
		}

		nameLabel.append(nameText);
		components.add(TooltipComponent.of(nameLabel.asOrderedText()));

		// Price
		var priceLabel = Text.literal(LocalizationUtil.localizedString("text", "offer.tooltip.price") + ": ")
				.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
		var priceValue = Text.literal(CurrencyFormattingUtil.formatCurrency(offer.price))
				.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xe2ca80)));
		var priceLine = priceLabel.append(priceValue);

		if (offer.stack.getCount() > 1) {
			// If the item stack has more than one item, show the per-item price breakdown
			var perItemLabel = Text
					.literal(" " + LocalizationUtil.localizedString("text", "offer.tooltip.price_breakdown",
							CurrencyFormattingUtil.formatCurrency(offer.price / offer.stack.getCount())))
					.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x989280)));
			priceLine = priceLine.append(perItemLabel);
		}

		components.add(TooltipComponent.of(priceLine.asOrderedText()));

		// Seller
		var sellerLabel = Text.literal(LocalizationUtil.localizedString("text", "offer.tooltip.seller") + ": ")
				.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
		var sellerValue = Text.literal(offer.sellerName).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
		components.add(TooltipComponent.of(sellerLabel.append(sellerValue).asOrderedText()));

		// Time calculations
		var currentTicks = world.getTime();
		var elapsedTicks = currentTicks - offer.timestamp;
		var timeExpiresTicks = Math.max(0, offer.timestamp + offer.duration - currentTicks);

		// Posted
		var rawPosted = TimeFormattingUtil.formattedTime(elapsedTicks);
		var postedFormatted = LocalizationUtil.localizedString("text", "offer.tooltip.time_posted_format",
				TextFormattingUtil.capitalizedString(rawPosted));
		var postedLabel = Text.literal(LocalizationUtil.localizedString("text", "offer.tooltip.time_posted") + ": ")
				.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
		var postedValue = Text.literal(postedFormatted).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
		components.add(TooltipComponent.of(postedLabel.append(postedValue).asOrderedText()));

		// Expiration
		var rawExpiry = TimeFormattingUtil.formattedTime(timeExpiresTicks);
		var expiryFormatted = LocalizationUtil.localizedString("text", "offer.tooltip.time_expiring_format", rawExpiry);
		var expiryLabel = Text.literal(LocalizationUtil.localizedString("text", "offer.tooltip.time_expiring") + ": ")
				.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
		var expiryValue = Text.literal(expiryFormatted).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
		components.add(TooltipComponent.of(expiryLabel.append(expiryValue).asOrderedText()));

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
			var generatedText = LocalizationUtil.localizedText("text", "offer.tooltip.generated_seller").copy();
			generatedText.setStyle(generatedText.getStyle().withItalic(true).withColor(Formatting.GRAY));
			components.add(TooltipComponent.of(generatedText.asOrderedText()));
		}

		return components;
	}

	// Total

	public static Text textForCartTotal(int value) {
		return Text.literal(CurrencyFormattingUtil.formatCurrency(value));
	}

	// Balance

	public static Text textForBalance(int value) {
		return Text.literal(CurrencyFormattingUtil.formatCurrency(value));
	}

	// Order

	private static final int MAX_ORDER_SUMMARY_ITEM_NAMES = 3;

	public static Text textForOrderSummary(List<Offer> offers) {
		if (offers.isEmpty()) {
			return LocalizationUtil.localizedText("gui", "order_summary_item_unknown");
		}

		var itemNames = offers.stream().map(offer -> {
			return ItemDescriptionUtil.descriptionForItemStack(offer.stack);
		}).distinct().toList();

		if (itemNames.size() <= MAX_ORDER_SUMMARY_ITEM_NAMES) {
			return Text.literal(String.join(", ", itemNames));
		}

		var displayableItemNames = itemNames.subList(0, MAX_ORDER_SUMMARY_ITEM_NAMES);
		var numberOfTruncatedItemNames = itemNames.size() - MAX_ORDER_SUMMARY_ITEM_NAMES;
		var listedItemNames = String.join(", ", displayableItemNames);

		if (numberOfTruncatedItemNames == 0) {
			return Text.literal(listedItemNames);
		} else {
			var moreText = LocalizationUtil.localizedString("gui", "order_item_more", numberOfTruncatedItemNames);
			return Text.literal(listedItemNames + ", " + moreText);
		}

	}

}
