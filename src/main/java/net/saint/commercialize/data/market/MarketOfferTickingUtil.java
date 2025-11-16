package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.bank.BankAccountAccessUtil;
import net.saint.commercialize.data.item.ItemSaleValueUtil;
import net.saint.commercialize.data.mail.MailTransitUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.player.PlayerProfileAccessUtil;
import net.saint.commercialize.data.text.ItemDescriptionUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;
import net.saint.commercialize.util.LocalizationUtil;

public final class MarketOfferTickingUtil {

	private enum SaleResult {
		SUCCESS,
		FAILURE_NO_SELLER,
		FAILURE_NO_ACCOUNT,
		FAILURE_UNKNOWN
	}

	// Logic

	public static void tickMarketOffersIfNecessary(World world) {
		var time = world.getTimeOfDay();

		if (time % Commercialize.CONFIG.offerCheckInterval == 0) {
			tickMarketOfferSaleGeneration(world);
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

		var numberOfOffersToGenerate = Math.max(
				0, Math.min(
						Commercialize.CONFIG.offerBatchSize,
						Commercialize.CONFIG.maxNumberOfOffers - Commercialize.MARKET_OFFER_MANAGER.size()
				)
		);

		for (int i = 0; i < numberOfOffersToGenerate; i++) {
			var generatedOffer = MarketOfferGenerator.generateOffer(world);
			generatedOffer.ifPresent(offer -> Commercialize.MARKET_OFFER_MANAGER.addOffer(offer));
		}
	}

	public static void tickMarketOfferSaleGeneration(World world) {
		var soldOffers = new ArrayList<Offer>();

		Commercialize.MARKET_OFFER_MANAGER.getOffers().forEach(offer -> {
			if (offer == null || !offer.isActive) {
				return;
			}

			if (world.getRandom().nextDouble() > Commercialize.CONFIG.offerSaleGenerationChance) {
				return;
			}

			var saleChance = calculateOfferSaleChance(offer);
			if (world.getRandom().nextFloat() > saleChance) {
				return;
			}

			var saleResult = generateOfferSale(world, offer);

			if (saleResult == SaleResult.SUCCESS) {
				soldOffers.add(offer);
			}
		});

		soldOffers.forEach(offer -> {
			Commercialize.MARKET_OFFER_MANAGER.removeOffer(offer.id);
		});
	}

	public static void tickMarketOfferExpiration(World world) {
		Commercialize.MARKET_OFFER_MANAGER.getOffers().filter(offer -> {
			return (world.getTimeOfDay() - offer.timestamp) >= offer.duration;
		}).collect(Collectors.toCollection(ArrayList::new)).forEach(offer -> {
			expireAndRemoveOffer(world, offer);
		});
	}

	// Sale

	private static SaleResult generateOfferSale(World world, Offer offer) {
		var server = world.getServer();

		if (offer.isGenerated) {
			return SaleResult.SUCCESS;
		}

		var sellerProfile = PlayerProfileAccessUtil.getPlayerProfileById(server, offer.sellerId);

		if (sellerProfile == null) {
			Commercialize.LOGGER.error(
					"Could not find player '{}' ({}) to pay out owed offer amount of {} ¤ after simulated sale of offer '{}'.",
					offer.sellerName, offer.sellerId, offer.price, offer.id
			);
			return SaleResult.FAILURE_NO_SELLER;
		}

		if (BankAccountAccessUtil.getBankAccountForPlayerById(sellerProfile.getId()) == null) {
			Commercialize.LOGGER.error(
					"Could not access bank account for player '{}' ({}) to pay out owed offer amount of {} ¤ for simulated sale of offer '{}'. The offer will not be sold.",
					offer.sellerName, offer.sellerId, offer.price, offer.id
			);
			return SaleResult.FAILURE_NO_ACCOUNT;
		}

		BankAccountAccessUtil.depositAccountBalanceForPlayer(sellerProfile.getId(), offer.price);
		MarketAnalyticsUtil.writeMarketOrderToAnalytics(offer, null);

		Commercialize.LOGGER.info(
				"Offer '{}' of {} has been purchased by a simulated player. Deposited {} ¤ to account of seller '{}' ({}).", offer.id,
				ItemDescriptionUtil.descriptionForItemStack(offer.stack), offer.price, offer.sellerName, offer.sellerId
		);

		return SaleResult.SUCCESS;
	}

	private static double calculateOfferSaleChance(Offer offer) {
		var intrinsicOfferValue = ItemSaleValueUtil.getValueForItemStack(offer.stack);

		if (intrinsicOfferValue == 0) {
			// Items that can not be estimated can not be sold to virtual players.
			return 0.0;
		}

		var offerTemplatePriceFactor = getOfferTemplatePriceFactorForStack(offer.stack);
		var marketOfferValue = intrinsicOfferValue * offerTemplatePriceFactor;
		var offerValueDivergence = offer.price / marketOfferValue;

		if (offerValueDivergence < 1.0) {
			return 1.0;
		}

		// Divergence moves from 1.0 up to 1.5 and sale should start at 100% and become less likely.
		var clampedDivergence = Math.min(offerValueDivergence, Commercialize.CONFIG.offerSaleGenerationMaxPriceFactor);
		var saleChance = 1.0 - (clampedDivergence - 1.0) / 0.5;

		return saleChance;
	}

	private static double getOfferTemplatePriceFactorForStack(ItemStack stack) {
		var itemIdentifier = Registries.ITEM.getId(stack.getItem());
		var offerTemplates = Commercialize.OFFER_TEMPLATE_MANAGER.getTemplatesForItem(itemIdentifier);

		if (offerTemplates.isEmpty()) {
			return 1.0;
		}

		var offerTemplateAverageMarkup = offerTemplates.stream()
				.map(template -> (double) template.markup)
				.reduce(0.0, Double::sum) / (double) offerTemplates.size();

		return offerTemplateAverageMarkup;
	}

	// Expiration

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
					offer.sellerId
			);
			return;
		}

		var didDispatch = MailTransitUtil.packageAndDispatchItemStacksToPlayer(server, playerId, itemList, packageMessage, packageSender);

		if (!didDispatch) {
			Commercialize.LOGGER.error(
					"Could not dispatch return expired offer items to player '{}' ({}).", offer.sellerName,
					offer.sellerId
			);
			return;
		}

		Commercialize.LOGGER.info(
				"Offer '{}' ({}) has expired and items were dispatched to seller '{}' ({}) via mail.", offer.id,
				itemDescription, offer.sellerName, offer.sellerId
		);
		Commercialize.MARKET_OFFER_MANAGER.removeOffer(offer);
	}

	private static void expireAndRemoveGeneratedOffer(World world, Offer offer) {
		Commercialize.MARKET_OFFER_MANAGER.removeOffer(offer);
	}

}
