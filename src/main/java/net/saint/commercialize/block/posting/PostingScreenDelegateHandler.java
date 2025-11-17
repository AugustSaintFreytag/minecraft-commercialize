package net.saint.commercialize.block.posting;

import net.minecraft.item.ItemStack;
import net.saint.commercialize.data.market.MarketPostingFeeUtils;
import net.saint.commercialize.screen.posting.OfferPostStrategy;
import net.saint.commercialize.screen.posting.PostingScreenDelegate;
import net.saint.commercialize.screen.posting.PostingScreenState;

public interface PostingScreenDelegateHandler extends PostingScreenDelegate {

	// State

	PostingScreenState getState();

	// Item

	default ItemStack getItemStack() {
		return getState().stack;
	}

	// Price

	default int getOfferPrice() {
		return getState().price;
	}

	default void updateOfferPrice(int price) {
		getState().price = price;
		onScreenUpdate();
	}

	// Duration

	default long getOfferDuration() {
		return getState().duration;
	}

	default void updateOfferDuration(long duration) {
		getState().duration = duration;
		onScreenUpdate();
	}

	// Post Strategy

	default OfferPostStrategy getPostStrategy() {
		return getState().postStrategy;
	}

	default void updatePostStrategy(OfferPostStrategy strategy) {
		getState().postStrategy = strategy;
		onEssentialsUpdate();
		onScreenUpdate();
	}

	// Fees

	default int getPostingFees() {
		return MarketPostingFeeUtils.calculatePostingFees(getItemStack(), getOfferPrice(), getOfferDuration(), getPostStrategy());
	}

	default boolean canAffordPostingFees() {
		return getState().balance >= getPostingFees();
	}

}
