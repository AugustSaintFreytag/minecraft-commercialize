package net.saint.commercialize.data.mail;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.text.ItemDescriptionUtil;
import net.saint.commercialize.util.LocalizationUtil;

public final class MailTransitUtil {

	public static void tickMailTransitIfNecessary(World world) {
		var time = world.getTime();

		if (time % Commercialize.CONFIG.mailDeliveryCheckInterval == 0) {
			tickMailTransit(world);
		}
	}

	public static void tickMailTransit(World world) {
		var server = world.getServer();
		var time = world.getTime();

		var transitItemsOutForDelivery = Commercialize.MAIL_TRANSIT_MANAGER.getItems().filter(item -> {
			return time > item.timeDispatched + Commercialize.CONFIG.mailDeliveryTime;
		}).filter(item -> {
			if (Commercialize.CONFIG.mailDeliveryChance == 1.0) {
				return true;
			}

			var roll = world.getRandom().nextDouble();
			return roll < Commercialize.CONFIG.mailDeliveryChance;
		});

		transitItemsOutForDelivery.forEach(transitItem -> {
			var playerId = transitItem.recipient;
			var playerName = playerNameForId(server, playerId);

			var itemStackDescriptions = ItemDescriptionUtil.descriptionForItemStack(transitItem.stack);

			if (!deliverItem(server, transitItem)) {
				Commercialize.LOGGER.warn("Could not deliver '{}' to mailbox of player '{}'. Item will remain in queue.",
						itemStackDescriptions, playerName);
				return;
			}

			Commercialize.LOGGER.info("Completed delivery of '{}' to mailbox of player '{}'.", itemStackDescriptions, playerName);
			Commercialize.MAIL_TRANSIT_MANAGER.removeItem(transitItem);
		});
	}

	private static String playerNameForId(MinecraftServer server, UUID playerId) {
		var playerProfile = server.getUserCache().getByUuid(playerId);

		if (!playerProfile.isPresent()) {
			return LocalizationUtil.localizedString("text", "player_unknown");
		}

		return playerProfile.get().getName();
	}

	private static boolean deliverItem(MinecraftServer server, MailTransitItem item) {
		var player = server.getPlayerManager().getPlayer(item.recipient);

		if (player == null) {
			Commercialize.LOGGER.warn("Could not find player '{}' for mail delivery.", item.recipient);
			return false;
		}

		return MailSystemAccessUtil.deliverItemStackToPlayerMailbox(server, player, item.stack);
	}

	// Dispatch

	public static boolean packageAndDispatchItemStacksToPlayer(MinecraftServer server, ServerPlayerEntity player,
			DefaultedList<ItemStack> itemStacks) {
		var world = server.getOverworld();
		var time = world.getTime();
		var packagedOrder = MailSystemAccessUtil.packageItemStacksForDelivery(itemStacks);
		var transitItem = new MailTransitItem(time, player.getUuid(), packagedOrder);

		Commercialize.MAIL_TRANSIT_MANAGER.pushItem(transitItem);
		return true;
	}

	public static boolean packageAndDeliverItemStacksToPlayer(MinecraftServer server, ServerPlayerEntity player,
			DefaultedList<ItemStack> itemStacks) {
		var packagedOrder = MailSystemAccessUtil.packageItemStacksForDelivery(itemStacks);
		return MailSystemAccessUtil.deliverItemStackToPlayerMailbox(server, player, packagedOrder);
	}

}
