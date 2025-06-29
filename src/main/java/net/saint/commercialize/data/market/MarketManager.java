package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.saint.commercialize.data.offer.Offer;

public final class MarketManager {

	// Properties

	private List<Offer> offers = new ArrayList<Offer>();

	// Access

	public Stream<Offer> getOffers() {
		return offers.stream();
	}

	public int size() {
		return offers.size();
	}

	// Mutation

	public void addOffer(Offer offer) {
		offers.add(offer);
	}

	public void clearOffers() {
		offers.clear();
	}

}
