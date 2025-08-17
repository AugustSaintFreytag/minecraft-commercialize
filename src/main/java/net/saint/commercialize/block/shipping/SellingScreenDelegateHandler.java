package net.saint.commercialize.block.shipping;

import net.minecraft.item.ItemStack;
import net.saint.commercialize.screen.selling.OfferPostStrategy;
import net.saint.commercialize.screen.selling.SellingScreenDelegate;
import net.saint.commercialize.screen.selling.SellingScreenState;

public interface SellingScreenDelegateHandler extends SellingScreenDelegate {

	// State

	SellingScreenState getState();

	// Item

	default ItemStack getItemStack() {
		return getState().selectedItem;
	}

	// Price

	default int getOfferPrice() {
		return getState().offerPrice;
	}

	default void updateOfferPrice(int price) {
		getState().offerPrice = price;
		onScreenUpdate();
	}

	// Duration

	default long getOfferDuration() {
		return getState().offerDuration;
	}

	default void updateOfferDuration(long duration) {
		getState().offerDuration = duration;
		onScreenUpdate();
	}

	// Post Strategy

	default OfferPostStrategy getPostStrategy() {
		return getState().offerPostStrategy;
	}

	default void updatePostStrategy(OfferPostStrategy strategy) {
		getState().offerPostStrategy = strategy;
		onScreenUpdate();
	}

}
