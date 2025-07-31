package net.saint.commercialize.screen.market;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;

public interface MarketScreenDelegate {

	// Player

	PlayerEntity getPlayer();

	int getBalance();

	boolean hasCardInHand();

	boolean hasOwnedCardInHand();

	String getCardOwnerName();

	// Cart

	List<Offer> getCart();

	void addOfferToCart(Offer offer);

	void removeOfferFromCart(Offer offer);

	boolean hasOfferInCart(Offer offer);

	int getCartTotal();

	void emptyCart();

	void confirmCartOrder();

	// Offers

	List<Offer> getOffers();

	boolean getOffersAreCapped();

	// Search

	String getSearchTerm();

	void setSearchTerm(String searchTerm);

	// Filtering & Sorting

	OfferSortMode getSortMode();

	void cycleSortMode();

	OfferSortOrder getSortOrder();

	void cycleSortOrder();

	OfferFilterMode getFilterMode();

	void cycleFilterMode();

	PaymentMethod getPaymentMethod();

	void cyclePaymentMethod();

}
