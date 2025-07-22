package net.saint.commercialize.block.market;

import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.bank.BankAccountAccessUtil;
import net.saint.commercialize.data.inventory.InventoryCashUtil;
import net.saint.commercialize.data.mail.MailSystemAccessUtil;
import net.saint.commercialize.data.market.MarketOfferListingUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.network.MarketC2SOrderMessage;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketC2SStateSyncMessage;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.network.MarketS2COrderMessage;

public final class MarketBlockServerNetworking {

	// Init

	public static void initialize() {

		ServerPlayNetworking.registerGlobalReceiver(MarketC2SStateSyncMessage.ID, (server, player, handler, buffer, responseSender) -> {
			var message = MarketC2SStateSyncMessage.decodeFromBuffer(buffer);

			server.execute(() -> {
				onReceiveMarketStateSync(server, player, responseSender, message);
			});
		});

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

	// Market State Sync Handler

	private static void onReceiveMarketStateSync(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
			MarketC2SStateSyncMessage message) {
		var world = player.getWorld();
		var blockEntity = world.getBlockEntity(message.position);

		if (!(blockEntity instanceof MarketBlockEntity)) {
			Commercialize.LOGGER.error("Could not mark market block entity at position {} as dirty, invalid type.", message.position);
			return;
		}

		var marketBlockEntity = (MarketBlockEntity) blockEntity;
		marketBlockEntity.setState(message.state);
	}

	// Market Data Request Handler

	private static void onReceiveMarketDataRequest(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
			MarketC2SQueryMessage message) {
		var maxNumberOfOffers = Commercialize.CONFIG.maxNumberOfListedItems;
		var allOffers = Commercialize.MARKET_MANAGER.getOffers();

		var preparedOffers = MarketOfferListingUtil.offersWithAppliedFilters(allOffers, player, message.filterMode, message.paymentMethod);
		var preparedOffersAreCapped = false;

		if (!message.searchTerm.isEmpty()) {
			preparedOffers = MarketOfferListingUtil.offersForSearchTerm(preparedOffers.stream(), player, message.searchTerm);
		}

		preparedOffers = MarketOfferListingUtil.offersWithAppliedSorting(preparedOffers, message.sortMode, message.sortOrder);

		if (preparedOffers.size() > maxNumberOfOffers) {
			// If offers are maximum size plus one, remove one and mark as capped.
			preparedOffersAreCapped = true;
			preparedOffers.remove(maxNumberOfOffers);
		}

		var balance = balanceForPlayerAndPaymentMethod(player, message.paymentMethod);

		sendMarketDataResponse(responseSender, message.position, balance, preparedOffers, preparedOffersAreCapped);
	}

	private static void sendMarketDataResponse(PacketSender responseSender, BlockPos position, int balance, List<Offer> offers,
			boolean offersAreCapped) {
		var responseMessage = new MarketS2CListMessage();

		responseMessage.position = position;
		responseMessage.balance = balance;
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
		var balance = balanceForPlayerAndPaymentMethod(player, message.paymentMethod);

		if (offerTotal > balance) {
			Commercialize.LOGGER.warn("Player '{}' tried to order offers for total price of '{}' ¤ but only has '{}' ¤ in inventory.",
					player.getName().getString(), offerTotal, balance);
			sendMarketOrderResponse(responseSender, message.position, message.offers, MarketS2COrderMessage.Result.INSUFFICIENT_FUNDS);
			return;
		}

		var didDispatch = dispatchOffersToPlayer(server, player, offers);

		if (!didDispatch) {
			Commercialize.LOGGER.warn("Player '{}' tried to order offers but order could not be dispatched.");
			sendMarketOrderResponse(responseSender, message.position, message.offers, MarketS2COrderMessage.Result.INVIABLE_DELIVERY);
			return;
		}

		deductAmountFromPlayerBalance(player, message.paymentMethod, offerTotal);
		removeOffers(offers);

		sendMarketOrderResponse(responseSender, message.position, message.offers, MarketS2COrderMessage.Result.SUCCESS);
	}

	private static boolean dispatchOffersToPlayer(MinecraftServer server, ServerPlayerEntity player, List<Offer> offers) {
		var itemStackList = itemStackListFromOffers(offers);

		// Mail Delivery

		if (Commercialize.CONFIG.useMailDelivery) {
			var packagedOrder = MailSystemAccessUtil.packageItemStacksForDelivery(itemStackList);
			var didSuccessfullyDeliverOrder = MailSystemAccessUtil.deliverItemStackToPlayerMailbox(server, player, packagedOrder);

			return didSuccessfullyDeliverOrder;
		}

		// Direct Delivery

		itemStackList.forEach(itemStack -> {
			player.giveItemStack(itemStack);
		});

		return true;
	}

	private static DefaultedList<ItemStack> itemStackListFromOffers(List<Offer> offers) {
		var itemStacks = offers.stream().map(offer -> offer.stack).toList();
		var itemStackList = DefaultedList.ofSize(itemStacks.size(), ItemStack.EMPTY);

		for (var index = 0; index < itemStacks.size(); index++) {
			itemStackList.set(index, itemStacks.get(index));
		}

		return itemStackList;
	}

	private static void removeOffers(List<Offer> offers) {
		offers.forEach(offer -> Commercialize.MARKET_MANAGER.removeOffer(offer));
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

	// Balance Utility

	private static int balanceForPlayerAndPaymentMethod(ServerPlayerEntity player, PaymentMethod paymentMethod) {
		switch (paymentMethod) {
		case INVENTORY:
			return InventoryCashUtil.getCurrencyValueInAnyInventoriesForPlayer(player);
		case ACCOUNT:
			return BankAccountAccessUtil.getBankAccountBalanceForPlayer(player);
		default:
			Commercialize.LOGGER.error("Requested player balance with invalid payment method '{}'.", paymentMethod);
			return 0;
		}
	}

	private static void deductAmountFromPlayerBalance(ServerPlayerEntity player, PaymentMethod paymentMethod, int amount) {
		switch (paymentMethod) {
		case INVENTORY:
			var remainingAmount = InventoryCashUtil.removeCurrencyFromInventory(player.getInventory(), amount);
			InventoryCashUtil.addCurrencyToAnyInventoriesForPlayer(player, -remainingAmount);
			break;
		case ACCOUNT:
			BankAccountAccessUtil.deductAccountBalanceForPlayer(player, amount);
			break;
		default:
			Commercialize.LOGGER.error("Requested transactional deduction with invalid payment method '{}'.", paymentMethod);
		}
	}

}
