package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.bank.BankAccountAccessUtil;
import net.saint.commercialize.data.inventory.InventoryCashUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;

public final class MarketOfferListingUtil {

	// Filtering

	public static List<Offer> offersWithAppliedFilters(Stream<Offer> offers, PlayerEntity player, OfferFilterMode filterMode,
			PaymentMethod paymentMethod) {
		if (filterMode == null) {
			filterMode = OfferFilterMode.ALL;
		}

		var maxNumberOfOffers = Commercialize.CONFIG.maxNumberOfListedItems + 1;

		switch (filterMode) {
			case AFFORDABLE:
				var balance = playerBalanceForPaymentMethod(player, paymentMethod);
				return offers.filter(offer -> offer.price <= balance).limit(maxNumberOfOffers).toList();
			default:
				return offers.limit(maxNumberOfOffers).toList();
		}
	}

	public static List<Offer> offersForSearchTerm(Stream<Offer> offers, PlayerEntity player, String searchTerm) {
		var maxNumberOfOffers = Commercialize.CONFIG.maxNumberOfListedItems + 1;

		if (searchTerm.isEmpty()) {
			return offers.limit(maxNumberOfOffers).toList();
		}

		var sanitizedSearchTerm = searchTerm.toLowerCase();

		return offers.filter(offer -> {
			var stack = offer.stack;
			var item = stack.getItem();
			var itemId = Registries.ITEM.getId(item);

			var itemName = stack.getName().getString().toLowerCase();
			var itemNamespace = itemId.getNamespace().toLowerCase();
			var itemTooltipLines = offer.stack.getTooltip(player, TooltipContext.BASIC).stream().map(line -> line.getString()).toList();
			var itemTooltip = String.join("", itemTooltipLines).toLowerCase();
			var sellerName = offer.sellerName.toLowerCase();

			var didMatch = itemName.contains(sanitizedSearchTerm) || itemNamespace.contains(sanitizedSearchTerm)
					|| itemTooltip.contains(sanitizedSearchTerm) || sellerName.contains(sanitizedSearchTerm);

			return didMatch;
		}).limit(maxNumberOfOffers).toList();
	}

	// Sorting

	public static List<Offer> offersWithAppliedSorting(List<Offer> offers, OfferSortMode sortMode, OfferSortOrder sortOrder) {
		var mutableOffers = new ArrayList<Offer>(offers);

		if (sortMode == null) {
			return mutableOffers;
		}

		switch (sortMode) {
			case TIME_POSTED:
				mutableOffers.sort(timePostedComparator());
				break;
			case PRICE:
				mutableOffers.sort(priceComparator());
				break;
			case ITEM_NAME:
				mutableOffers.sort(itemNameComparator());
				break;
			case PLAYER_NAME:
				mutableOffers.sort(playerNameComparator());
				break;
			default:
				break;
		}

		if (sortOrder == OfferSortOrder.DESCENDING) {
			Collections.reverse(mutableOffers);
		}

		return mutableOffers;
	}

	// Sorting Comparators

	private static Comparator<Offer> timePostedComparator() {
		return (lhs, rhs) -> Long.compare(rhs.timestamp, lhs.timestamp);
	}

	private static Comparator<Offer> priceComparator() {
		return (lhs, rhs) -> Integer.compare(lhs.price, rhs.price);
	}

	private static Comparator<Offer> itemNameComparator() {
		return (lhs, rhs) -> {
			var nameComparison = lhs.stack.getName().getString().compareTo(rhs.stack.getName().getString());

			if (nameComparison != 0) {
				return nameComparison;
			}

			return Integer.compare(lhs.stack.getCount(), rhs.stack.getCount());
		};
	}

	private static Comparator<Offer> playerNameComparator() {
		return (lhs, rhs) -> {
			var sellerComparison = lhs.sellerName.compareTo(rhs.sellerName);

			if (sellerComparison != 0) {
				return sellerComparison;
			}

			var itemNameComparison = lhs.stack.getName().getString().compareTo(rhs.stack.getName().getString());

			if (itemNameComparison != 0) {
				return itemNameComparison;
			}

			return Integer.compare(lhs.stack.getCount(), rhs.stack.getCount());
		};
	}

	private static int playerBalanceForPaymentMethod(PlayerEntity player, PaymentMethod paymentMethod) {
		switch (paymentMethod) {
			case INVENTORY:
				return InventoryCashUtil.getCurrencyValueInAnyInventoriesForPlayer(player);
			case ACCOUNT:
				return BankAccountAccessUtil.getBankAccountBalanceForPlayer(player);
			default:
				return 0;
		}
	}

}
