package net.saint.commercialize.block;

import java.util.ArrayList;
import java.util.List;

import net.saint.commercialize.data.market.MarketManager;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;

public class MarketBlockEntityScreenState {

	// Market

	public MarketManager marketManager = new MarketManager();

	// Cart

	public List<Offer> cart = new ArrayList<>();

	// Filtering & Sorting

	public String searchTerm = "";
	public OfferSortMode sortMode = OfferSortMode.ITEM_NAME;
	public OfferSortOrder sortOrder = OfferSortOrder.ASCENDING;
	public OfferFilterMode filterMode = OfferFilterMode.ALL;
	public PaymentMethod paymentMethod = PaymentMethod.INVENTORY;

}
