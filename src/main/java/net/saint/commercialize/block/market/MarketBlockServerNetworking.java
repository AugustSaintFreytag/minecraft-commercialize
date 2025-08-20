package net.saint.commercialize.block.market;

import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.bank.BankAccountAccessUtil;
import net.saint.commercialize.data.inventory.InventoryCashUtil;
import net.saint.commercialize.data.mail.MailSystemAccessUtil;
import net.saint.commercialize.data.mail.MailTransitUtil;
import net.saint.commercialize.data.market.MarketOfferListingUtil;
import net.saint.commercialize.data.market.MarketPlayerUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.data.text.ItemDescriptionUtil;
import net.saint.commercialize.init.ModSounds;
import net.saint.commercialize.network.MarketC2SOrderMessage;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketC2SStateSyncMessage;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.network.MarketS2COrderMessage;
import net.saint.commercialize.util.LocalizationUtil;

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
		var position = message.position;
		var blockEntity = world.getBlockEntity(position);

		if (!(blockEntity instanceof MarketBlockEntity)) {
			Commercialize.LOGGER.error("Could not resolve market block entity at position {}, invalid type '{}'.", position,
					blockEntity.getClass().getName());
			return;
		}

		var marketBlockEntity = (MarketBlockEntity) blockEntity;
		marketBlockEntity.setState(message.state);

		handleMarketInteraction(server, player, message.reason, position, marketBlockEntity);
	}

	private static void handleMarketInteraction(MinecraftServer server, ServerPlayerEntity player, MarketBlockStateSyncReason reason,
			BlockPos position, MarketBlockEntity blockEntity) {
		var world = player.getWorld();

		switch (reason) {
			case INTERACTION_START: {
				world.playSound(null, position, ModSounds.MARKET_OPEN_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
				var playerHoldsPaymentCard = BankAccountAccessUtil.isPaymentCard(player.getMainHandStack());

				if (playerHoldsPaymentCard) {
					world.playSound(null, position, ModSounds.CARD_INSERT_SOUND, SoundCategory.BLOCKS, 0.75f, 1.0f);
				}

				break;
			}
			case INTERACTION_END: {
				world.playSound(null, position, ModSounds.MARKET_CLOSE_SOUND, SoundCategory.BLOCKS, 0.5f, 1.0f);

				var playerHoldsPaymentCard = BankAccountAccessUtil.isPaymentCard(player.getMainHandStack());

				if (playerHoldsPaymentCard) {
					world.playSound(null, position, ModSounds.CARD_EJECT_SOUND, SoundCategory.BLOCKS, 0.75f, 1.0f);
				}

				break;
			}
			default: {
				break;
			}
		}
	}

	// Market Data Request Handler

	private static void onReceiveMarketDataRequest(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
			MarketC2SQueryMessage message) {
		var maxNumberOfOffers = Commercialize.CONFIG.maxNumberOfListedItems;
		var preparedOffers = MarketOfferListingUtil.offersWithAppliedQuery(player, message);
		var preparedOffersAreCapped = false;

		if (preparedOffers.size() > maxNumberOfOffers) {
			// If offers are maximum size plus one, remove one and mark as capped.
			preparedOffersAreCapped = true;
			preparedOffers.remove(maxNumberOfOffers);
		}

		var balance = balanceForPlayerAndPaymentMethod(player, message.paymentMethod);
		var cardOwner = cardOwnerForItemHeldByPlayer(player);

		sendMarketDataResponse(responseSender, message.position, balance, cardOwner, preparedOffers, preparedOffersAreCapped);
	}

	private static void sendMarketDataResponse(PacketSender responseSender, BlockPos position, int balance, String cardOwner,
			List<Offer> offers, boolean offersAreCapped) {
		var responseMessage = new MarketS2CListMessage();

		responseMessage.position = position;
		responseMessage.balance = balance;
		responseMessage.cardOwner = cardOwner;
		responseMessage.offers = offers;
		responseMessage.isCapped = offersAreCapped;

		var responseBuffer = PacketByteBufs.create();
		responseMessage.encodeToBuffer(responseBuffer);

		responseSender.sendPacket(MarketS2CListMessage.ID, responseBuffer);
	}

	// Market Order Request Handler

	private static void onReceiveMarketOrderRequest(MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender,
			MarketC2SOrderMessage message) {
		var offers = offersFromList(message.offers);

		if (offers.size() != message.offers.size()) {
			Commercialize.LOGGER
					.warn("Could not collect and prepare all requested offers from market. Some orders may be invalid or have expired.");
			sendMarketOrderResponse(responseSender, message.position, message.offers, MarketS2COrderMessage.Result.INVIABLE_OFFERS);
			return;
		}

		if (Commercialize.CONFIG.requireCardForMarketPayment && message.paymentMethod == PaymentMethod.ACCOUNT) {
			Commercialize.LOGGER.warn(
					"Player '{}' tried to order offers with payment method 'ACCOUNT' while configuration forbids direct-from-account payment.",
					player.getName().getString());
			sendMarketOrderResponse(responseSender, message.position, message.offers, MarketS2COrderMessage.Result.INVIABLE_PAYMENT_METHOD);
			return;
		}

		if (!Commercialize.CONFIG.allowForeignCardsForMarketPayment && message.paymentMethod == PaymentMethod.SPECIFIED_ACCOUNT) {
			var cardOwner = cardOwnerForItemHeldByPlayer(player);

			if (!cardOwner.equals(player.getName().getString())) {
				Commercialize.LOGGER.warn(
						"Player '{}' tried to order offers with a payment card belonging to '{}' but configuration forbids foreign card payment.",
						player.getName().getString(), cardOwner);
				sendMarketOrderResponse(responseSender, message.position, message.offers,
						MarketS2COrderMessage.Result.INVIABLE_PAYMENT_METHOD);
				return;
			}
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
		payOutOfferAmountsToSellers(server, offers);
		removeOffers(offers);

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

	// Actions

	private static boolean dispatchOffersToPlayer(MinecraftServer server, ServerPlayerEntity player, List<Offer> offers) {
		var playerCanReceiveDeliveries = MailSystemAccessUtil.getMailboxForPlayer(server, player) != null;

		if (!playerCanReceiveDeliveries) {
			return false;
		}

		var itemStackList = itemStackListFromOffers(offers);
		return dispatchItemStacksToPlayer(server, player, itemStackList);
	}

	private static boolean dispatchItemStacksToPlayer(MinecraftServer server, ServerPlayerEntity player,
			DefaultedList<ItemStack> itemStacks) {
		if (!Commercialize.CONFIG.useMailDelivery) {
			itemStacks.forEach(itemStack -> {
				player.giveItemStack(itemStack);
			});

			return true;

		}

		var packageMessage = messageForPackagedDelivery(itemStacks);
		var packageSender = LocalizationUtil.localizedString("text", "delivery.sender");

		return MailTransitUtil.packageAndDispatchItemStacksToPlayer(server, player, itemStacks, packageMessage, packageSender);
	}

	private static String messageForPackagedDelivery(List<ItemStack> itemStacks) {
		var itemStackDescriptions = itemStacks.stream().map(stack -> ItemDescriptionUtil.descriptionForItemStack(stack)).toList();
		var itemStackDescription = String.join(", ", itemStackDescriptions);
		var packageMessage = LocalizationUtil.localizedString("text", "delivery.receipt_format", itemStackDescription) + "\n\n"
				+ LocalizationUtil.localizedString("text", "delivery.signature");

		return packageMessage;
	}

	private static List<Offer> offersFromList(List<UUID> list) {
		return list.stream().map(offerId -> Commercialize.MARKET_OFFER_MANAGER.getOffer(offerId)).flatMap(java.util.Optional::stream)
				.toList();
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
		offers.forEach(offer -> Commercialize.MARKET_OFFER_MANAGER.removeOffer(offer));
	}

	// Bank Account Utility

	private static int balanceForPlayerAndPaymentMethod(ServerPlayerEntity player, PaymentMethod paymentMethod) {
		switch (paymentMethod) {
			case INVENTORY:
				return InventoryCashUtil.getCurrencyValueInAnyInventoriesForPlayer(player);
			case ACCOUNT:
				return BankAccountAccessUtil.getBankAccountBalanceForPlayer(player);
			case SPECIFIED_ACCOUNT:
				var heldItemStack = player.getMainHandStack();
				return BankAccountAccessUtil.getBankAccountBalanceForCard(heldItemStack);
			default:
				Commercialize.LOGGER.error("Requested player balance with invalid payment method '{}'.", paymentMethod);
				return 0;
		}
	}

	private static String cardOwnerForItemHeldByPlayer(ServerPlayerEntity player) {
		var heldItemStack = player.getMainHandStack();
		var ownerName = BankAccountAccessUtil.getOwnerNameForCard(heldItemStack);

		if (ownerName == null) {
			return "";
		}

		return ownerName;
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
			case SPECIFIED_ACCOUNT:
				var heldItemStack = player.getMainHandStack();
				BankAccountAccessUtil.deductAccountBalanceForCard(heldItemStack, amount);
				break;
			default:
				Commercialize.LOGGER.error("Requested transactional deduction with invalid payment method '{}'.", paymentMethod);
		}
	}

	private static void payOutOfferAmountsToSellers(MinecraftServer server, List<Offer> offers) {
		for (var offer : offers) {
			if (offer.isGenerated) {
				// Generated offers will be skipped, no seller to pay out to.
				continue;
			}

			var seller = MarketPlayerUtil.playerEntityForId(server, offer.sellerId);

			if (seller == null) {
				Commercialize.LOGGER.error("Could not find player '{}' ({}) to pay out owed offer amount of {} ¤ after sale of offer '{}'.",
						offer.sellerName, offer.sellerId, offer.price, offer.id);
				continue;
			}

			BankAccountAccessUtil.depositAccountBalanceForPlayer(seller, offer.price);
			Commercialize.LOGGER.info("Paid player '{}' ({}) an amount of {} ¤ for sale of offer '{}'.", offer.sellerName, offer.sellerId,
					offer.price, offer.id);
		}
	}

}
