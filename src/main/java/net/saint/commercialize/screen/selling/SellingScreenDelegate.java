package net.saint.commercialize.screen.selling;

import net.minecraft.item.ItemStack;

public interface SellingScreenDelegate {

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

	SellingPostStrategy getPostStrategy();

	void updatePostStrategy(SellingPostStrategy strategy);

}
