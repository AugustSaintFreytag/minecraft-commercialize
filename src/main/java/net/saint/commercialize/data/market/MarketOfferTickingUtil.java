package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.mail.MailTransitUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.text.ItemDescriptionUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;
import net.saint.commercialize.util.localization.LocalizationUtil;

public final class MarketOfferTickingUtil {

	// Logic

	public static void tickMarketOffersIfNecessary(World world) {
		var time = world.getTimeOfDay();

		if (time % Commercialize.CONFIG.offerCheckInterval == 0) {
			tickMarketOfferGeneration(world);
		}

		if (time % Commercialize.CONFIG.offerGenerationTickInterval == 0) {
			tickMarketOfferExpiration(world);
		}
	}

	public static void tickMarketOfferGeneration(World world) {
		if (Commercialize.MARKET_OFFER_MANAGER.size() >= Commercialize.CONFIG.maxNumberOfOffers) {
			return;
		}

		var numberOfOffersToGenerate = Math.max(0, Math.min(Commercialize.CONFIG.offerBatchSize,
				Commercialize.CONFIG.maxNumberOfOffers - Commercialize.MARKET_OFFER_MANAGER.size()));

		for (int i = 0; i < numberOfOffersToGenerate; i++) {
			var generatedOffer = MarketOfferGenerator.generateOffer(world);
			generatedOffer.ifPresent(offer -> Commercialize.MARKET_OFFER_MANAGER.addOffer(offer));
		}
	}

	public static void tickMarketOfferExpiration(World world) {
		Commercialize.MARKET_OFFER_MANAGER.getOffers().filter(offer -> {
			return (world.getTimeOfDay() - offer.timestamp) >= offer.duration;
		}).collect(Collectors.toCollection(ArrayList::new)).forEach(offer -> {
			expireAndRemoveOffer(world, offer);
		});
	}

	public static void expireAndRemoveOffer(World world, Offer offer) {
		if (!offer.isGenerated) {
			expireAndRemovePlayerOffer(world, offer);
		} else {
			expireAndRemoveGeneratedOffer(world, offer);
		}
	}

	private static void expireAndRemovePlayerOffer(World world, Offer offer) {
		// Offer is owned by real player, send items back to player mailbox.
		var itemList = DefaultedList.ofSize(1, ItemStack.EMPTY);
		itemList.set(0, offer.stack);

		var itemDescription = ItemDescriptionUtil.textForItemStack(offer.stack);
		var packageSender = LocalizationUtil.localizedText("text", "delivery.market");
		var packageReceipt = LocalizationUtil.localizedText("text", "return.offer_format", itemDescription);
		var packageSignature = LocalizationUtil.localizedText("text", "return.message", TimeFormattingUtil.formattedTime(offer.duration));

		var packageMessage = packageReceipt.copy().append(Text.of("\n\n")).append(packageSignature);

		var server = world.getServer();
		var playerId = offer.sellerId;

		if (!MarketPlayerUtil.isKnownPlayerId(server, playerId)) {
			Commercialize.LOGGER.error(
					"Could not verify player '{}' ({}) as a known player on the server to return expired offer items to.", offer.sellerName,
					offer.sellerId);
			return;
		}

		var didDispatch = MailTransitUtil.packageAndDispatchItemStacksToPlayer(server, playerId, itemList, packageMessage, packageSender);

		if (!didDispatch) {
			Commercialize.LOGGER.error("Could not dispatch return expired offer items to player '{}' ({}).", offer.sellerName,
					offer.sellerId);
			return;
		}

		Commercialize.LOGGER.info("Offer '{}' ({}) has expired and items were dispatched to seller '{}' ({}) via mail.", offer.id,
				itemDescription, offer.sellerName, offer.sellerId);
		Commercialize.MARKET_OFFER_MANAGER.removeOffer(offer);
	}

	private static void expireAndRemoveGeneratedOffer(World world, Offer offer) {
		Commercialize.MARKET_OFFER_MANAGER.removeOffer(offer);
	}

}
