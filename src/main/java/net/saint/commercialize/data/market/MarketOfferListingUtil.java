package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.network.MarketC2SQueryMessage;

public final class MarketOfferListingUtil {

	public static List<Offer> offersWithAppliedQuery(ServerPlayerEntity player, MarketC2SQueryMessage message) {
		var maxNumberOfOffers = Commercialize.CONFIG.maxNumberOfListedItems;
		var sortedOffers = Commercialize.MARKET_OFFER_CACHE_MANAGER.getOffersWithAppliedSorting(message.sortMode, message.sortOrder);

		return sortedOffers.filter(MarketOfferFilteringUtil.offerFilterPredicate(player, message.filterMode, message.paymentMethod))
				.filter(MarketOfferFilteringUtil.searchFilterPredicate(player, message.searchTerm)).limit(maxNumberOfOffers + 1)
				.collect(Collectors.toCollection(ArrayList::new));
	}

}
