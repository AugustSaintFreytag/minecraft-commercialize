package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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

	public void removeOffer(Offer offer) {
		offers.remove(offer);
	}

	public void removeOffer(UUID id) {
		offers.removeIf(offer -> offer.id.equals(id));
	}

	public void removeOffers(Collection<Offer> offers) {
		this.offers.removeAll(offers);
	}

	public void addOffers(Collection<Offer> offers) {
		this.offers.addAll(offers);
	}

	public void clearOffers() {
		offers.clear();
	}

}
