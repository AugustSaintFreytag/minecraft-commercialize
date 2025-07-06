package net.saint.commercialize.block;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.market.MarketOfferListingUtil;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketS2CListMessage;

public final class MarketBlockServerNetworking {

	// Init

	public static void initialize() {
		// Listen for market query requests sent from client.
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
	}

	// Handlers

	private static void onReceiveMarketDataRequest(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
			MarketC2SQueryMessage message) {
		var offers = MarketOfferListingUtil.offersWithAppliedFilters(Commercialize.MARKET_MANAGER.getOffers(), player, message.filterMode);
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

		var responseMessage = new MarketS2CListMessage();

		responseMessage.position = message.position;
		responseMessage.offers = offers;
		responseMessage.isCapped = offersAreCapped;

		var responseBuffer = PacketByteBufs.create();
		responseMessage.encodeToBuffer(responseBuffer);

		responseSender.sendPacket(MarketS2CListMessage.ID, responseBuffer);
	}

}
