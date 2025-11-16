package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.text.TimePreset;
import net.saint.commercialize.screen.posting.OfferPostStrategy;

public final class MarketOfferPostingUtil {

	// Library

	public static enum OfferPostingResult {
		SUCCESS, OUT_OF_QUOTA, INVALID, FAILURE
	}

	public static record OfferDraft(ItemStack stack, int price, long duration, OfferPostStrategy strategy) {
	}

	// Properties

	private static AtomicReference<HashMap<UUID, Integer>> dailyNumberOfPostsByPlayer = new AtomicReference<>(new HashMap<>());

	// Posting

	public static OfferPostingResult postOfferToMarket(MinecraftServer server, ServerPlayerEntity player, OfferDraft draft) {
		if (!validatePlayerCanPostWithinQuota(player, draft)) {
			return OfferPostingResult.OUT_OF_QUOTA;
		}

		if (!validateOfferDraft(draft)) {
			return OfferPostingResult.INVALID;
		}

		var offers = makeOffersFromDraft(player, draft);

		Commercialize.MARKET_OFFER_MANAGER.addOffers(offers);
		updatePostQuotaForPlayer(player, offers.size());

		Commercialize.LOGGER.info("Player '{}' posted {} offer(s) to market.", player.getName().getString(), offers.size());

		return OfferPostingResult.SUCCESS;
	}

	// Offer Form

	private static List<Offer> makeOffersFromDraft(ServerPlayerEntity player, OfferDraft draft) {
		switch (draft.strategy()) {
			case AS_STACK:
				return List.of(makeIndividualOfferFromDraft(player, draft));
			case AS_ITEMS:
				return makeBatchOffersFromDraft(player, draft);
			default:
				Commercialize.LOGGER.error("Can not make offers for market posting with invalid posting strategy '{}'.", draft.strategy());
				return List.of();
		}
	}

	private static List<Offer> makeBatchOffersFromDraft(ServerPlayerEntity player, OfferDraft draft) {
		var offers = new ArrayList<Offer>();

		for (int i = 0; i < draft.stack().getCount(); i++) {
			var singleStack = new ItemStack(draft.stack().getItem(), 1);
			singleStack.setNbt(draft.stack().getOrCreateNbt().copy());

			var singleDraft = new OfferDraft(singleStack, draft.price(), draft.duration(), draft.strategy());
			var offer = makeIndividualOfferFromDraft(player, singleDraft);

			offers.add(offer);
		}

		return offers;
	}

	private static Offer makeIndividualOfferFromDraft(ServerPlayerEntity player, OfferDraft draft) {
		var world = player.getWorld();
		var offer = new Offer();

		offer.id = UUID.randomUUID();

		offer.isActive = true;
		offer.isGenerated = false;

		offer.sellerId = player.getUuid();
		offer.sellerName = player.getName().getString();

		offer.timestamp = world.getTimeOfDay();
		offer.duration = draft.duration();

		offer.stack = draft.stack();
		offer.price = draft.price();

		return offer;
	}

	// Validation

	private static boolean validatePlayerCanPostWithinQuota(ServerPlayerEntity player, OfferDraft draft) {
		var remainingPostQuota = Commercialize.CONFIG.maxNumberOfPlayerOffersPerDay - usedPostQuotaForPlayer(player);

		if (remainingPostQuota <= 0) {
			return false;
		}

		if (draft.strategy() == OfferPostStrategy.AS_ITEMS) {
			return remainingPostQuota >= draft.stack().getCount();
		}

		return true;
	}

	private static int usedPostQuotaForPlayer(ServerPlayerEntity player) {
		return dailyNumberOfPostsByPlayer.get().getOrDefault(player.getUuid(), 0);
	}

	private static void updatePostQuotaForPlayer(ServerPlayerEntity player, int addedNumberOfPosts) {
		dailyNumberOfPostsByPlayer.updateAndGet(map -> {
			var currentCount = map.getOrDefault(player.getUuid(), 0);
			map.put(player.getUuid(), currentCount + addedNumberOfPosts);

			return map;
		});
	}

	private static boolean validateOfferDraft(OfferDraft draft) {
		try {
			return draft.duration() > 0 && draft.duration() < TimePreset.twoWeeks() && draft.price() > 0
					&& draft.price() < Integer.MAX_VALUE;
		} catch (Exception exception) {
			return false;
		}
	}

}
