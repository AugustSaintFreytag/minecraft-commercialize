package net.saint.commercialize.data.market;

import net.minecraft.item.ItemStack;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.text.TimePreset;
import net.saint.commercialize.screen.posting.OfferPostStrategy;

public final class MarketPostingFeeUtils {

	public static int calculatePostingFees(ItemStack itemStack, int offerPrice, long offerDuration, OfferPostStrategy postStrategy) {
		if (itemStack.isEmpty()) {
			return 0;
		}

		var basePrice = offerPrice;

		if (postStrategy == OfferPostStrategy.AS_ITEMS) {
			basePrice *= itemStack.getCount();
		}

		var priceFeeFactor = Commercialize.CONFIG.postingFeePriceFactor;
		var timeFeeFactor = Commercialize.CONFIG.postingFeeTimeFactor;

		if (priceFeeFactor == 0 || timeFeeFactor == 0) {
			return 0;
		}

		var offerDurationDays = Math.max(0, (double) offerDuration / (double) TimePreset.oneDay());
		var offerFeeRate = priceFeeFactor + timeFeeFactor * Math.sqrt(offerDurationDays);
		var rawOfferFees = (int) (basePrice * offerFeeRate);
		// var roundedOfferFees = Math.max(10, (int) (Math.floor((double) rawOfferFees / 10.0) * 10.0));

		return rawOfferFees;
	}

}
