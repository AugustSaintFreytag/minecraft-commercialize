package net.saint.commercialize.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketS2CListMessage;

public class ModNetworking {

	private static final int MAX_OFFERS_PER_REQUEST = 100;

	public static void initialize() {
		ServerPlayNetworking.registerGlobalReceiver(MarketC2SQueryMessage.ID, (server, player, handler, buffer, responseSender) -> {
			var message = MarketC2SQueryMessage.decodeFromBuffer(buffer);
			server.execute(() -> {
				onReceiveMarketDataRequest(server, player, responseSender, message);
			});
		});
	}

	private static void onReceiveMarketDataRequest(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
			MarketC2SQueryMessage message) {
		var offers = offersWithAppliedFilters(Commercialize.MARKET_MANAGER.getOffers(), message.filterMode);
		var offersAreCapped = false;

		offers = offersWithAppliedSorting(offers, message.sortMode, message.sortOrder);

		if (offers.size() > MAX_OFFERS_PER_REQUEST) {
			// If offers are maximum size plus one, remove one and mark as capped.
			offersAreCapped = true;
			offers.remove(MAX_OFFERS_PER_REQUEST);
		}

		var responseMessage = new MarketS2CListMessage();
		responseMessage.isCapped = offersAreCapped;
		responseMessage.offers = offers;

		var responseBuffer = PacketByteBufs.create();
		responseMessage.encodeToBuffer(responseBuffer);

		responseSender.sendPacket(MarketS2CListMessage.ID, responseBuffer);
	}

	private static List<Offer> offersWithAppliedFilters(Stream<Offer> offers, OfferFilterMode filterMode) {
		if (filterMode == null) {
			filterMode = OfferFilterMode.ALL;
		}

		switch (filterMode) {
		case AFFORDABLE:
			// TODO: Implement once monetary data is available.
			return new ArrayList<Offer>();
		default:
			return offers.limit(MAX_OFFERS_PER_REQUEST + 1).toList();
		}
	}

	private static List<Offer> offersWithAppliedSorting(List<Offer> offers, OfferSortMode sortMode, OfferSortOrder sortOrder) {
		var mutableOffers = new ArrayList<Offer>(offers);

		if (sortMode == null) {
			return mutableOffers;
		}

		switch (sortMode) {
		case TIME_POSTED:
			mutableOffers.sort((lhs, rhs) -> {
				return Long.compare(rhs.timestamp, lhs.timestamp);
			});
			break;
		case PRICE:
			mutableOffers.sort((lhs, rhs) -> {
				return Integer.compare(lhs.price, rhs.price);
			});
			break;
		case ITEM_NAME:
			mutableOffers.sort((lhs, rhs) -> {
				var nameComparison = lhs.stack.getName().getString().compareTo(rhs.stack.getName().getString());

				if (nameComparison != 0) {
					return nameComparison;
				}

				return Integer.compare(lhs.stack.getCount(), rhs.stack.getCount());
			});
			break;
		case PLAYER_NAME:
			mutableOffers.sort((lhs, rhs) -> {
				var sellerComparison = lhs.sellerName.compareTo(rhs.sellerName);

				if (sellerComparison != 0) {
					return sellerComparison;
				}

				var itemNameComparison = lhs.stack.getName().getString().compareTo(rhs.stack.getName().getString());

				if (itemNameComparison != 0) {
					return itemNameComparison;
				}

				return Integer.compare(lhs.stack.getCount(), rhs.stack.getCount());
			});
			break;
		default:
			break;
		}

		if (sortOrder == OfferSortOrder.DESCENDING) {
			Collections.reverse(mutableOffers);
		}

		return mutableOffers;
	}

}
