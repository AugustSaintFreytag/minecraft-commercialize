package net.saint.commercialize.block;

import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.inventory.PlayerInventoryCashUtil;
import net.saint.commercialize.data.market.MarketOfferListingUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.network.MarketC2SOrderMessage;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.network.MarketS2COrderMessage;

public final class MarketBlockServerNetworking {

	// Init

	public static void initialize() {

		ServerPlayNetworking.registerGlobalReceiver(MarketC2SQueryMessage.ID, (server, player, handler, buffer, responseSender) -> {
			try {
				var message = MarketC2SQueryMessage.decodeFromBuffer(buffer);

				server.execute(() -> {
					onReceiveMarketDataRequest(server, player, responseSender, message);
				});
			} catch (Exception e) {
				Commercialize.LOGGER.error("Could not decode and forward market data request from player '{}'.",
						player.getName().getString(), e);
				return;
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(MarketC2SOrderMessage.ID, (server, player, handler, buffer, responseSender) -> {
			try {
				var message = MarketC2SOrderMessage.decodeFromBuffer(buffer);

				server.execute(() -> {
					onReceiveMarketOrderRequest(server, player, responseSender, message);
				});
			} catch (Exception e) {
				Commercialize.LOGGER.error("Could not decode and forward market data request from player '{}'.",
						player.getName().getString(), e);
				return;
			}
		});
	}

	// Market Data Request Handler

	private static void onReceiveMarketDataRequest(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
			MarketC2SQueryMessage message) {
		var offers = MarketOfferListingUtil.offersWithAppliedFilters(Commercialize.MARKET_MANAGER.getOffers(), player, message.filterMode,
				message.paymentMethod);
		var offersAreCapped = false;

		if (!message.searchTerm.isEmpty()) {
			offers = MarketOfferListingUtil.offersForSearchTerm(offers.stream(), player, message.searchTerm);
		}

		offers = MarketOfferListingUtil.offersWithAppliedSorting(offers, message.sortMode, message.sortOrder);

		if (offers.size() > MarketOfferListingUtil.MAX_OFFERS_PER_LISTING) {
			// If offers are maximum size plus one, remove one and mark as capped.
			offersAreCapped = true;
			offers.remove(MarketOfferListingUtil.MAX_OFFERS_PER_LISTING);
		}

		sendMarketDataResponse(responseSender, message.position, offers, offersAreCapped);
	}

	private static void sendMarketDataResponse(PacketSender responseSender, BlockPos position, List<Offer> offers,
			boolean offersAreCapped) {
		var responseMessage = new MarketS2CListMessage();

		responseMessage.position = position;
		responseMessage.offers = offers;
		responseMessage.isCapped = offersAreCapped;

		var responseBuffer = PacketByteBufs.create();
		responseMessage.encodeToBuffer(responseBuffer);

		responseSender.sendPacket(MarketS2CListMessage.ID, responseBuffer);
	}

	// Market Order Request Handler

	private static void onReceiveMarketOrderRequest(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
			MarketC2SOrderMessage message) {
		var offers = message.offers.stream().map(offerId -> Commercialize.MARKET_MANAGER.getOffer(offerId))
				.flatMap(java.util.Optional::stream).toList();

		if (offers.size() != message.offers.size()) {
			Commercialize.LOGGER
					.warn("Could not collect and prepare all requested offers from market. Some orders may be invalid or have expired.");
			sendMarketOrderResponse(responseSender, message.position, message.offers, MarketS2COrderMessage.Result.INVIABLE_OFFERS);
			return;
		}

		var offerTotal = offers.stream().mapToInt(offer -> offer.price).sum();
		var playerBalance = PlayerInventoryCashUtil.getCurrencyValueInAnyInventoriesForPlayer(player);

		if (offerTotal > playerBalance) {
			Commercialize.LOGGER.warn("Player '{}' tried to order offers for total price of '{}' ¤ but only has '{}' ¤ in inventory.",
					player.getName().getString(), offerTotal, playerBalance);
			sendMarketOrderResponse(responseSender, message.position, message.offers, MarketS2COrderMessage.Result.INSUFFICIENT_FUNDS);
			return;
		}

		var remainingAmount = PlayerInventoryCashUtil.removeCurrencyFromInventory(player.getInventory(), offerTotal);
		PlayerInventoryCashUtil.addCurrencyToAnyInventoriesForPlayer(player, -remainingAmount);

		offers.forEach(offer -> {
			player.giveItemStack(offer.stack);
			Commercialize.MARKET_MANAGER.removeOffer(offer);
		});

		sendMarketOrderResponse(responseSender, message.position, message.offers, MarketS2COrderMessage.Result.SUCCESS);
	}

	private static void sendMarketOrderResponse(PacketSender responseSender, BlockPos position, List<UUID> offers,
			MarketS2COrderMessage.Result result) {
		var responseMessage = new MarketS2COrderMessage();

		responseMessage.position = position;
		responseMessage.offers = offers;
		responseMessage.result = result;

		var responseBuffer = PacketByteBufs.create();
		responseMessage.encodeToBuffer(responseBuffer);

		responseSender.sendPacket(MarketS2COrderMessage.ID, responseBuffer);

	}

}
