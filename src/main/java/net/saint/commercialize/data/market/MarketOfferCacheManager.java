package net.saint.commercialize.data.market;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;

public class MarketOfferCacheManager {

	// Library

	public record Predicate(OfferSortMode sortMode, OfferSortOrder sortOrder) {
	}

	// Properties

	private Map<Predicate, List<Offer>> offersByPredicate = new HashMap<>();

	// Access

	public void clear() {
		offersByPredicate.clear();
	}

	public Stream<Offer> getOffersWithAppliedSorting(OfferSortMode sortMode, OfferSortOrder sortOrder) {
		var predicate = new Predicate(sortMode, sortOrder);

		// If exact requested predicate already exists, return stream.
		if (offersByPredicate.containsKey(predicate)) {
			return offersByPredicate.get(predicate).stream();
		}

		// If flipped sort order already exists, retrieve, flip, cache, return.
		var flippedPredicate = flippedOrderPredicate(predicate);

		if (offersByPredicate.containsKey(flippedPredicate)) {
			var offers = offersByPredicate.get(flippedPredicate).reversed();
			offersByPredicate.put(predicate, offers);

			return offers.stream();
		}

		// If no cached entry exists, compute, cache, return.
		var offers = MarketOfferSortingUtil.offersWithAppliedSorting(Commercialize.MARKET_OFFER_MANAGER.getOffers().toList(), sortMode,
				sortOrder);
		offersByPredicate.put(predicate, offers);

		return offers.stream();
	}

	// Utility

	private static Predicate flippedOrderPredicate(Predicate predicate) {
		return new Predicate(predicate.sortMode(), flippedSortOrder(predicate.sortOrder()));
	}

	private static OfferSortOrder flippedSortOrder(OfferSortOrder sortOrder) {
		switch (sortOrder) {
			case ASCENDING:
				return OfferSortOrder.DESCENDING;
			case DESCENDING:
				return OfferSortOrder.ASCENDING;
			default:
				return sortOrder;
		}
	}

}
