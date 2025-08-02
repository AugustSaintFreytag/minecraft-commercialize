package net.saint.commercialize.block.market;

import java.util.Objects;

import net.saint.commercialize.data.market.MarketOfferCollection;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;

public class MarketBlockEntityScreenState {

	// Market

	public MarketOfferCollection marketOffers = new MarketOfferCollection();

	// Cart

	public MarketOfferCollection cartOffers = new MarketOfferCollection();

	// Balance

	public int balance = 0;

	public String cardOwner = "";

	// Filtering & Sorting

	public String searchTerm = "";

	public OfferSortMode sortMode = OfferSortMode.ITEM_NAME;
	public OfferSortOrder sortOrder = OfferSortOrder.ASCENDING;
	public OfferFilterMode filterMode = OfferFilterMode.ALL;
	public PaymentMethod paymentMethod = PaymentMethod.INVENTORY;

	// Convenience

	public int marketOffersHashCode() {
		return marketOffers.hashCode();
	}

	public int cartOffersHashCode() {
		return cartOffers.hashCode();
	}

	public int viewPropertiesHashCode() {
		return Objects.hash(searchTerm, sortMode, sortOrder, filterMode, paymentMethod);
	}

}
