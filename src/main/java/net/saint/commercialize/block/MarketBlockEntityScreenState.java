package net.saint.commercialize.block;

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

	// Filtering & Sorting

	public String searchTerm = "";
	public OfferSortMode sortMode = OfferSortMode.ITEM_NAME;
	public OfferSortOrder sortOrder = OfferSortOrder.ASCENDING;
	public OfferFilterMode filterMode = OfferFilterMode.ALL;
	public PaymentMethod paymentMethod = PaymentMethod.INVENTORY;

}
