package net.saint.commercialize.data.market;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.common.StackSizeRange;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferTemplate;
import net.saint.commercialize.util.RandomPlayerUtil;

public final class MarketOfferGenerator {

	// Configuration

	private static final int[] STACK_SIZES = { 1, 2, 4, 8, 16, 24, 36, 48, 64 };

	private static final double SELLING_FACTOR = 0.75;
	private static final double BUYING_FACTOR = 1.15;
	private static final double JITTER_FACTOR = 0.085;

	private static final int OFFER_DURATION = 36_000;

	// Properties

	private static Map<StackSizeRange, int[]> cachedStackSizeArrayByRange = new HashMap<>();

	private static Random random = Random.create();

	// Generation

	public static Offer generateOffer(World world) {
		var offerTemplate = getRandomOfferTemplate(random);

		var offer = new Offer();
		var itemStack = getItemStackForOfferTemplate(random, offerTemplate);
		var price = getTotalPriceForItemStack(random, itemStack);
		var sellerName = RandomPlayerUtil.randomPlayerName(random);

		if (price == 0) {
			return null;
		}

		offer.id = UUID.randomUUID();
		offer.isActive = true;
		offer.isGenerated = true;
		offer.sellerId = null;
		offer.sellerName = sellerName;
		offer.timestamp = world.getTime();
		offer.duration = OFFER_DURATION;
		offer.stack = itemStack;
		offer.price = price;

		return offer;
	}

	// Price

	private static int getTotalPriceForItemStack(Random random, ItemStack itemStack) {
		var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
		var itemBaseValue = Commercialize.ITEM_MANAGER.getValueForItem(itemIdentifier);

		if (itemBaseValue == 0) {
			Commercialize.LOGGER.warn("Could not get total price for requested item '{}'.", itemIdentifier);
			return 0;
		}

		var stackSize = itemStack.getCount();
		var rawValue = ((double) itemBaseValue) * BUYING_FACTOR * stackSize;
		var jitterValue = random.nextTriangular(0, JITTER_FACTOR) * itemBaseValue;

		rawValue += jitterValue;

		return roundToNearestAesthetic((int) rawValue);
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
	private static OfferTemplate getRandomOfferTemplate(Random random) {
		return Commercialize.OFFER_TEMPLATE_MANAGER.getRandomTemplate(random);
	}

	private static int getRandomStackSize(Random random, StackSizeRange range) {
		if (range == null) {
			return 1;
		}

		var stackSizes = cachedStackSizeArrayByRange.computeIfAbsent(range, _range -> {
			return stackSizeArrayForRange(range);
		});

		var index = random.nextInt(stackSizes.length);
		return stackSizes[index];
	}

	private static int[] stackSizeArrayForRange(StackSizeRange range) {
		return Arrays.stream(STACK_SIZES).filter(value -> value >= range.min && value <= range.max).toArray();
	}

}
