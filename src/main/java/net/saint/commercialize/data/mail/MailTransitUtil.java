package net.saint.commercialize.data.mail;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.market.MarketPlayerUtil;
import net.saint.commercialize.data.text.ItemDescriptionUtil;

public final class MailTransitUtil {

	// Ticking

	public static void tickMailTransitIfNecessary(World world) {
		var time = world.getTimeOfDay();

		if (time % Commercialize.CONFIG.mailDeliveryCheckInterval == 0) {
			tickMailTransit(world);
		}
	}

	public static void tickMailTransit(World world) {
		var server = world.getServer();
		var time = world.getTimeOfDay();

		var itemsOutForDelivery = Commercialize.MAIL_TRANSIT_MANAGER.getItems().filter(item -> {
			// Final delivery time of the package under ideal conditions.
			var deliveryTime = item.timeDispatched + Commercialize.CONFIG.mailDeliveryTime;

			// Add hold time if a package could not be delivered and is now being held.
			var holdTime = item.numberOfDeliveryAttempts * Commercialize.CONFIG.mailDeliveryTime;

			return time > deliveryTime + holdTime;
		}).filter(item -> {
			if (Commercialize.CONFIG.mailDeliveryChance == 1.0) {
				return true;
			}

			var roll = world.getRandom().nextDouble();
			return roll < Commercialize.CONFIG.mailDeliveryChance;
		}).filter(item -> {
			if (!Commercialize.CONFIG.suspendDeliveryAttemptsForOfflinePlayers) {
				return true;
			}

			var player = MarketPlayerUtil.getPlayerEntityForId(server, item.recipient);
			var playerIsOnline = player != null;

			return playerIsOnline;
		});

		var itemsNotDeliveredByPlayer = new HashMap<UUID, List<MailTransitItem>>();
		var itemsToBeDiscardedByPlayer = new HashMap<UUID, List<MailTransitItem>>();

		itemsOutForDelivery.forEach(item -> {
			var playerId = item.recipient;
			var playerName = MarketPlayerUtil.getPlayerNameForId(server, playerId);

			var itemStackDescriptions = ItemDescriptionUtil.descriptionForItemStackWithCount(item.stack);

			if (!deliverMailTransitItem(server, item)) {
				item.timeLastDeliveryAttempted = time;
				item.numberOfDeliveryAttempts++;

				if (item.numberOfDeliveryAttempts > Commercialize.CONFIG.maxNumberOfDeliveryAttempts) {
					itemsToBeDiscardedByPlayer.computeIfAbsent(playerId, k -> new java.util.ArrayList<>()).add(item);
					Commercialize.MAIL_TRANSIT_MANAGER.removeItem(item);

					Commercialize.LOGGER.info(
							"Could not deliver '{}' to mailbox of player '{}' after {} attempt(s).",
							itemStackDescriptions,
							playerName,
							item.numberOfDeliveryAttempts + 1
					);
					return;
				}

				Commercialize.LOGGER.info(
						"Could not deliver '{}' to mailbox of player '{}' after {} attempt(s). Item will remain in queue.",
						itemStackDescriptions,
						playerName,
						item.numberOfDeliveryAttempts
				);

				itemsNotDeliveredByPlayer.computeIfAbsent(playerId, k -> new java.util.ArrayList<>()).add(item);
				Commercialize.MAIL_TRANSIT_MANAGER.updateItem(item);

				return;
			}

			Commercialize.LOGGER.info("Completed delivery of '{}' to mailbox of player '{}'.", itemStackDescriptions, playerName);
			Commercialize.MAIL_TRANSIT_MANAGER.removeItem(item);
		});

		if (Commercialize.CONFIG.notifyPlayersOfDeliveryAttempts) {
			MailTransitNotificationUtil.notifyPlayersOfFailedDeliveryAttempts(server, itemsNotDeliveredByPlayer);
			MailTransitNotificationUtil.notifyPlayersOfDeliveryDiscard(server, itemsToBeDiscardedByPlayer);
		}
	}

	public static void forceDeliverAllMailTransitItems(MinecraftServer server) {
		Commercialize.MAIL_TRANSIT_MANAGER.getItems().forEach(item -> {
			var playerName = MarketPlayerUtil.getPlayerNameForId(server, item.recipient);
			var itemStackDescriptions = ItemDescriptionUtil.descriptionForItemStackWithCount(item.stack);
			var didDeliverItem = deliverMailTransitItem(server, item);

			if (!didDeliverItem) {
				Commercialize.LOGGER.warn(
						"Could not force-deliver mail item with '{}' to mailbox of player '{}'.",
						itemStackDescriptions,
						playerName
				);
				return;
			}

			Commercialize.LOGGER.info("Force-delivered mail item with '{}' to mailbox of player '{}'.", itemStackDescriptions, playerName);
			Commercialize.MAIL_TRANSIT_MANAGER.removeItem(item);
		});
	}

	// Delivery

	public static boolean deliverMailTransitItem(MinecraftServer server, MailTransitItem item) {
		var player = MarketPlayerUtil.getPlayerEntityForId(server, item.recipient);

		if (player == null) {
			Commercialize.LOGGER.warn("Could not find offline player '{}' for mail delivery.", item.recipient);
			return false;
		}

		return MailSystemAccessUtil.deliverItemStackToPlayerMailbox(server, player, item.stack);
	}

	// Dispatch

	public static boolean packageAndDispatchItemStacksToPlayer(MinecraftServer server, UUID playerId, DefaultedList<ItemStack> itemStacks,
			Text message, Text sender) {
		var world = server.getOverworld();
		var time = world.getTimeOfDay();
		var packagedOrder = MailSystemAccessUtil.packageItemStacksForDelivery(itemStacks, message, sender);
		var item = new MailTransitItem(time, playerId, packagedOrder);

		Commercialize.MAIL_TRANSIT_MANAGER.pushItem(item);
		return true;
	}

	public static boolean packageAndDeliverItemStacksToPlayer(MinecraftServer server, ServerPlayerEntity player,
			DefaultedList<ItemStack> itemStacks, Text message, Text sender) {
		var packagedOrder = MailSystemAccessUtil.packageItemStacksForDelivery(itemStacks, message, sender);
		return MailSystemAccessUtil.deliverItemStackToPlayerMailbox(server, player, packagedOrder);
	}

}
