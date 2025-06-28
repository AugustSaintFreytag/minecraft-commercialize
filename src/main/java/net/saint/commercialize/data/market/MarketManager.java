package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.List;

import net.saint.commercialize.data.offer.Offer;

public final class MarketManager {

	// Properties

	private List<Offer> offers = new ArrayList<Offer>();

	// Access

	public List<Offer> getOffers() {
		return offers;
	}

	// Mutation

	public void addOffer(Offer offer) {
		offers.add(offer);
	}

	public void clearOffers() {
		offers.clear();
	}

}
