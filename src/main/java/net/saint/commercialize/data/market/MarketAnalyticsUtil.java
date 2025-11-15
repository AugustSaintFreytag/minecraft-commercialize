package net.saint.commercialize.data.market;

import com.mojang.authlib.GameProfile;

import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;

public final class MarketAnalyticsUtil {

	public static void writeMarketOrderToAnalytics(Offer offer, GameProfile buyerProfile) {

		var buyerReport = Commercialize.MARKET_ANALYTICS_MANAGER.getOrCreateReportForProfile(buyerProfile);
		buyerReport.numberOfOrders += 1;
		buyerReport.amountSpentOnOrders += offer.price;

		var sellerProfile = new GameProfile(offer.sellerId, offer.sellerName);
		var sellerReport = Commercialize.MARKET_ANALYTICS_MANAGER.getOrCreateReportForProfile(sellerProfile);
		sellerReport.numberOfSales += 1;
		sellerReport.amountEarnedFromSales += offer.price;

		Commercialize.MARKET_ANALYTICS_MANAGER.markDirty();
	}

}
