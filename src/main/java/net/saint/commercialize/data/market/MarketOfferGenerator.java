package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.common.StackSizeRange;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferTemplate;
import net.saint.commercialize.data.player.RandomPlayerNameUtil;

public final class MarketOfferGenerator {

	// Configuration

	private static final int[] STACK_SIZES = { 1, 2, 4, 8, 16, 24, 32, 48, 64 };

	private static List<String> playerPool = new ArrayList<String>();

	// Properties

	private static Map<StackSizeRange, int[]> cachedStackSizeArrayByRange = new HashMap<>();

	private static Random random = Random.create();

	// Generation

	public static Optional<Offer> generateOffer(World world) {
		var offerTemplateOptional = getRandomOfferTemplate(random);

		if (offerTemplateOptional.isEmpty()) {
			Commercialize.LOGGER.warn(
					"Could not generate offer, no offer template returned from random selection. Potentially no offer templates available in registry.");
			return Optional.empty();
		}

		var offerTemplate = offerTemplateOptional.get();

		var offer = new Offer();
		var itemStack = getItemStackForOfferTemplate(random, offerTemplate);
		var price = getSellingPriceForOffer(random, offerTemplate, itemStack);

		if (price == 0) {
			Commercialize.LOGGER.warn("Could not generate offer for item '{}' with zero price, returning null.",
					itemStack.getItem().getName().getString());
			return Optional.empty();
		}

		var sellerId = Offer.GENERATED_SELLER_ID;
		var sellerName = getRandomPlayerName();

		offer.id = UUID.randomUUID();
		offer.isActive = true;
		offer.isGenerated = true;
		offer.sellerId = sellerId;
		offer.sellerName = sellerName;
		offer.timestamp = world.getTimeOfDay();
		offer.duration = Commercialize.CONFIG.offerDuration;
		offer.stack = itemStack;
		offer.price = price;

		return Optional.of(offer);
	}

	// Player

	private static String getRandomPlayerName() {
		if (playerPool.isEmpty()) {
			pregenerateNames();
		}

		var index = random.nextInt(playerPool.size());
		return playerPool.get(index);
	}

	public static void pregenerateNames() {
		playerPool.clear();

		// Generate a pool of random player names to use for offers
		for (int i = 0; i < 6; i++) {
			var name = RandomPlayerNameUtil.randomPlayerName(random);
			playerPool.add(name);
		}
	}

	public static void clearPregeneratedNames() {
		playerPool.clear();
	}

	// Price

	private static int getSellingPriceForOffer(Random random, OfferTemplate offerTemplate, ItemStack itemStack) {
		var price = getSellingPriceForItemStack(random, itemStack);
		var markup = offerTemplate.markup;

		return roundToNearestAesthetic((int) (price * markup));
	}

	private static int getSellingPriceForItemStack(Random random, ItemStack itemStack) {
		var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
		var itemBaseValue = Commercialize.ITEM_MANAGER.getValueForItem(itemIdentifier);

		if (itemBaseValue == 0) {
			Commercialize.LOGGER.warn("Could not get total price for requested item '{}'.", itemIdentifier);
			return 0;
		}

		var stackSize = itemStack.getCount();
		var stackValue = ((double) itemBaseValue) * Commercialize.CONFIG.buyingPriceFactor * ((double) stackSize);
		var jitterFactor = random.nextTriangular(0, Commercialize.CONFIG.priceJitterFactor);

		stackValue *= jitterFactor;

		return Math.max(0, (int) Math.round(stackValue));
	}

	private static int roundToNearestAesthetic(int value) {
		// Round any price above 10 to nearest 8, 16, 32, 64
		if (value >= 10) {
			if (value % 8 == 0) {
				return value;
			} else if (value % 16 == 0) {
				return value;
			} else if (value % 32 == 0) {
				return value;
			} else if (value % 64 == 0) {
				return value;
			} else {
				return ((value + 7) / 8) * 8; // Round up to nearest multiple of 8
			}
		} else {
			return value; // For values below 10, return as is
		}
	}

	// Item Stack

	private static ItemStack getItemStackForOfferTemplate(Random random, OfferTemplate offerTemplate) {
		var stackSize = getRandomStackSize(random, offerTemplate.stack);
		var item = Registries.ITEM.get(offerTemplate.item);
		var itemStack = new ItemStack(item, stackSize);

		return itemStack;
	}

	// Random Selection

	/**
	 * Picks a random offer template from the offer template manager.
	 */
	private static Optional<OfferTemplate> getRandomOfferTemplate(Random random) {
		return Commercialize.OFFER_TEMPLATE_MANAGER.getRandomTemplate(random);
	}

	private static int getRandomStackSize(Random random, StackSizeRange range) {
		if (range == null) {
			return 1;
		}

		var stackSizes = cachedStackSizeArrayByRange.computeIfAbsent(range, _range -> {
			return stackSizeArrayForRange(range);
		});

		if (stackSizes.length == 0) {
			return range.min;
		}

		if (stackSizes.length == 1) {
			return stackSizes[0];
		}

		var index = random.nextInt(stackSizes.length);
		return stackSizes[index];
	}

	private static int[] stackSizeArrayForRange(StackSizeRange range) {
		return Arrays.stream(STACK_SIZES).filter(value -> value >= range.min && value <= range.max).toArray();
	}

}
