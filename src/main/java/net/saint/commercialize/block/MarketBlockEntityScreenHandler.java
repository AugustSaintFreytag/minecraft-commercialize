package net.saint.commercialize.block;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.saint.commercialize.data.inventory.PlayerInventoryCashUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.screen.market.MarketScreenDelegate;

public interface MarketBlockEntityScreenHandler extends MarketScreenDelegate {

	// State

	MarketBlockEntityScreenState getState();

	// Event

	void onMarketScreenUpdate();

	// Player

	@Override
	default PlayerEntity getPlayer() {
		var client = MinecraftClient.getInstance();
		return client.player;
	}

	@Override
	default int getCashBalance() {
		return PlayerInventoryCashUtil.getCurrencyValueInAnyInventoriesForPlayer(getPlayer());
	}

	@Override
	default int getAccountBalance() {
		return 0;
	}

	// Cart

	@Override
	default List<Offer> getCart() {
		return getState().cartOffers.getOffers().toList();
	}

	@Override
	default boolean hasOfferInCart(Offer offer) {
		var cart = getState().cartOffers;
		return cart.hasOffer(offer.id);
	}

	@Override
	default void addOfferToCart(Offer offer) {
		var cart = getState().cartOffers;

		if (cart.hasOffer(offer.id)) {
			// If offer is already in cart, do not add again.
			return;
		}

		cart.addOffer(offer);
		onMarketScreenUpdate();
	}

	@Override
	default void removeOfferFromCart(Offer offer) {
		var cart = getState().cartOffers;

		cart.removeOffer(offer);
		onMarketScreenUpdate();
	}

	@Override
	default void emptyCart() {
		var cart = getState().cartOffers;

		cart.clearOffers();
		onMarketScreenUpdate();
	}

	@Override
	default int getCartTotal() {
		var total = getCart().stream().mapToInt(offer -> offer.price).sum();
		return total;
	}

	// Offers

	@Override
	default List<Offer> getOffers() {
		return getState().marketOffers.getOffers().toList();
	}

	@Override
	default boolean getOffersAreCapped() {
		return getState().marketOffers.offersAreCapped();
	}

	// Filtering & Sorting

	@Override
	default String getSearchTerm() {
		return getState().searchTerm;
	}

	@Override
	default void setSearchTerm(String searchTerm) {
		getState().searchTerm = searchTerm;
		onMarketScreenUpdate();
	}

	@Override
	default OfferSortMode getSortMode() {
		return getState().sortMode;
	}

	@Override
	default void setSortMode(OfferSortMode sortMode) {
		getState().sortMode = sortMode;
		onMarketScreenUpdate();
	}

	@Override
	default OfferSortOrder getSortOrder() {
		return getState().sortOrder;
	}

	@Override
	default void setSortOrder(OfferSortOrder sortOrder) {
		getState().sortOrder = sortOrder;
		onMarketScreenUpdate();
	}

	@Override
	default OfferFilterMode getFilterMode() {
		return getState().filterMode;
	}

	@Override
	default void setFilterMode(OfferFilterMode filterMode) {
		getState().filterMode = filterMode;
		onMarketScreenUpdate();
	}

	@Override
	default PaymentMethod getPaymentMethod() {
		return getState().paymentMethod;
	}

	@Override
	default void setPaymentMethod(PaymentMethod paymentMethod) {
		getState().paymentMethod = paymentMethod;
		onMarketScreenUpdate();
	}
}