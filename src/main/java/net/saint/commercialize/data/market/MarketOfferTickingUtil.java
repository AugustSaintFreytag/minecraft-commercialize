package net.saint.commercialize.data.market;

import java.util.ArrayList;

import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;

public final class MarketOfferTickingUtil {

	// Configuration

	/**
	 * If this many offers are active on the market, no further generated offers are added.
	 */
	private static final int MAX_NUMBER_OF_OFFERS = 100;

	private static final int OFFER_BATCH_SIZE = 6;

	/**
	 * The interval in ticks at which the market checks for expired offers to be removed.
	 */
	private static final int OFFER_CHECK_INTERVAL = 100;

	/**
	 * The interval in ticks at which new offers are attempted to be generated.
	 */
	private static final int OFFER_GENERATION_TICK_INTERVAL = 2_000;

	// State

	private static long lastOfferGeneration = 0;
	private static long lastOfferExpiration = 0;

	// Logic

	public static void tickMarketOffersIfNecessary(World world) {
		var time = world.getTime();

		if (time > lastOfferGeneration + OFFER_CHECK_INTERVAL) {
			lastOfferGeneration = time;
			tickMarketOfferGeneration(world);
		}

		if (time > lastOfferExpiration + OFFER_GENERATION_TICK_INTERVAL) {
			lastOfferExpiration = time;
			tickMarketOfferExpiration(world);
		}
	}

	public static void tickMarketOfferGeneration(World world) {
		if (Commercialize.MARKET_MANAGER.size() >= MAX_NUMBER_OF_OFFERS) {
			return;
		}

		var numberOfOffersToGenerate = Math.max(0, Math.min(OFFER_BATCH_SIZE, MAX_NUMBER_OF_OFFERS - Commercialize.MARKET_MANAGER.size()));

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
