package net.saint.commercialize.screen.market;

import java.util.ArrayList;
import java.util.List;

import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;

public interface MarketScreenDelegate {

	// Cart

	ArrayList<Offer> getCart();

	void addOfferToCart(Offer offer);

	void removeOfferFromCart(Offer offer);

	void emptyCart();

	// Offers

	List<Offer> getOffers();

	boolean getOffersAreCapped();

	// Filtering & Sorting

	String getSearchTerm();

	void setSearchTerm(String searchTerm);

	OfferSortMode getSortMode();

	void setSortMode(OfferSortMode sortMode);

	OfferSortOrder getSortOrder();

	void setSortOrder(OfferSortOrder sortOrder);

	OfferFilterMode getFilterMode();

	void setFilterMode(OfferFilterMode filterMode);

	PaymentMethod getPaymentMethod();

	void setPaymentMethod(PaymentMethod paymentMethod);

}
