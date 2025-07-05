package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;

public final class MarketOfferListingUtil {

	// Configuration

	public static final int MAX_OFFERS_PER_LISTING = 100;

	// Filtering

	public static List<Offer> offersWithAppliedFilters(Stream<Offer> offers, OfferFilterMode filterMode) {
		if (filterMode == null) {
			filterMode = OfferFilterMode.ALL;
		}

		switch (filterMode) {
		case AFFORDABLE:
			// TODO: Implement once monetary data is available.
			return new ArrayList<Offer>();
		default:
			return offers.limit(MAX_OFFERS_PER_LISTING + 1).toList();
		}
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

}
