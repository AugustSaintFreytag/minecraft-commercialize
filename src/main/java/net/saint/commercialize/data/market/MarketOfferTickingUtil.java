package net.saint.commercialize.data.market;

import java.util.ArrayList;

import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;

public final class MarketOfferTickingUtil {

	// State

	private static long lastOfferGeneration = 0;
	private static long lastOfferExpiration = 0;

	// Logic

	public static void tickMarketOffersIfNecessary(World world) {
		var time = world.getTime();

		if (time > lastOfferGeneration + Commercialize.CONFIG.offerCheckInterval) {
			lastOfferGeneration = time;
			tickMarketOfferGeneration(world);
		}

		if (time > lastOfferExpiration + Commercialize.CONFIG.offerGenerationTickInterval) {
			lastOfferExpiration = time;
			tickMarketOfferExpiration(world);
		}
	}

	public static void tickMarketOfferGeneration(World world) {
		if (Commercialize.MARKET_MANAGER.size() >= Commercialize.CONFIG.maxNumberOfOffers) {
			return;
		}

		var numberOfOffersToGenerate = Math.max(0, Math.min(Commercialize.CONFIG.offerBatchSize,
				Commercialize.CONFIG.maxNumberOfOffers - Commercialize.MARKET_MANAGER.size()));

		for (int i = 0; i < numberOfOffersToGenerate; i++) {
			var generatedOffer = MarketOfferGenerator.generateOffer(world);
			generatedOffer.ifPresent(offer -> Commercialize.MARKET_MANAGER.addOffer(offer));
		}
	}

	public static void tickMarketOfferExpiration(World world) {
		var expiredOffers = new ArrayList<Offer>();

		Commercialize.MARKET_MANAGER.getOffers().filter(offer -> (world.getTime() - offer.timestamp) >= offer.duration).forEach(offer -> {
			expiredOffers.add(offer);
		});

		expiredOffers.forEach(offer -> {
			Commercialize.MARKET_MANAGER.removeOffer(offer);
		});
	}

}
