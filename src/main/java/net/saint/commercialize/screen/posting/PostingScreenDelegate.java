package net.saint.commercialize.screen.posting;

import net.minecraft.item.ItemStack;

public interface PostingScreenDelegate {

	// Event

	void onScreenClose();

	void onScreenUpdate();

	// Actions

	void confirmOfferPost();

	void clearOfferPost();

	void resetOfferPrice();

	// Item

	ItemStack getItemStack();

	// Price

	int getOfferPrice();

	void updateOfferPrice(int price);

	// Duration

	long getOfferDuration();

	void updateOfferDuration(long duration);

	// Post Strategy

	OfferPostStrategy getPostStrategy();

	void updatePostStrategy(OfferPostStrategy strategy);

}
