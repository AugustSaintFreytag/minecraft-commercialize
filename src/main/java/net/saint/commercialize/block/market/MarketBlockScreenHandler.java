package net.saint.commercialize.block.market;

import static net.saint.commercialize.util.Values.assertedValueInSequence;
import static net.saint.commercialize.util.Values.nextValueInSequence;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.bank.BankAccountAccessUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.screen.market.MarketScreenDelegate;
import net.saint.commercialize.util.LocalizationUtil;

public interface MarketBlockScreenHandler extends MarketScreenDelegate {

	// State

	MarketBlockEntityScreenState getState();

	// Player

	@Override
	default PlayerEntity getPlayer() {
		var client = MinecraftClient.getInstance();
		return client.player;
	}

	@Override
	default int getBalance() {
		return getState().balance;
	}

	@Override
	default boolean hasCardInHand() {
		var heldItemStack = getPlayer().getMainHandStack();
		return BankAccountAccessUtil.isPaymentCard(heldItemStack);
	}

	@Override
	default boolean hasOwnedCardInHand() {
		var player = getPlayer();
		var playerName = player.getName().getString();
		var cardOwnerName = getCardOwnerName();

		return playerName == cardOwnerName;
	}

	@Override
	default String getCardOwnerName() {
		var ownerName = getState().cardOwner;

		if (ownerName == null || ownerName.isEmpty()) {
			return LocalizationUtil.localizedString("gui", "market.player_unknown");
		}

		return ownerName;
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

	// Search Term

	@Override
	default String getSearchTerm() {
		return getState().searchTerm;
	}

	@Override
	default void setSearchTerm(String searchTerm) {
		getState().searchTerm = searchTerm;
		onMarketScreenUpdate();
	}

	// Sort Mode

	@Override
	default OfferSortMode getSortMode() {
		return getState().sortMode;
	}

	@Override
	default void cycleSortMode() {
		var state = getState();
		state.sortMode = nextValueInSequence(getSupportedSortModes(), state.sortMode);

		onMarketScreenUpdate();
	}

	// Sort Order

	@Override
	default OfferSortOrder getSortOrder() {
		return getState().sortOrder;
	}

	@Override
	default void cycleSortOrder() {
		var state = getState();
		state.sortOrder = nextValueInSequence(getSupportedSortOrders(), state.sortOrder);

		onMarketScreenUpdate();
	}

	// Filter Mode

	@Override
	default OfferFilterMode getFilterMode() {
		return getState().filterMode;
	}

	@Override
	default void cycleFilterMode() {
		var state = getState();
		state.filterMode = nextValueInSequence(getSupportedFilterModes(), state.filterMode);

		onMarketScreenUpdate();
	}

	// Payment Method

	@Override
	default PaymentMethod getPaymentMethod() {
		var state = getState();
		state.paymentMethod = assertedValueInSequence(getSupportedPaymentMethods(), state.paymentMethod);

		return state.paymentMethod;
	}

	@Override
	default void cyclePaymentMethod() {
		var state = getState();
		state.paymentMethod = nextValueInSequence(getSupportedPaymentMethods(), state.paymentMethod);

		onMarketScreenUpdate();
	}

	// Ordered Properties

	private OfferSortMode[] getSupportedSortModes() {
		return OfferSortMode.values();
	}

	private OfferFilterMode[] getSupportedFilterModes() {
		return OfferFilterMode.values();
	}

	private OfferSortOrder[] getSupportedSortOrders() {
		return OfferSortOrder.values();
	}

	private PaymentMethod[] getSupportedPaymentMethods() {
		if (hasCardInHand()) {
			return new PaymentMethod[] { PaymentMethod.SPECIFIED_ACCOUNT };
		}

		if (Commercialize.CONFIG.requireCardForMarketPayment) {
			return new PaymentMethod[] { PaymentMethod.INVENTORY };
		}

		return new PaymentMethod[] { PaymentMethod.INVENTORY, PaymentMethod.ACCOUNT };
	}

}