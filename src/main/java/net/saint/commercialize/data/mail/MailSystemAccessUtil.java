package net.saint.commercialize.data.mail;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mrcrayfish.furniture.refurbished.mail.DeliveryService;
import com.mrcrayfish.furniture.refurbished.mail.Mailbox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.mixin.DeliveryServiceAccessor;

public final class MailSystemAccessUtil {

	// Delivery

	public static ItemStack packageItemStacksForDelivery(List<ItemStack> itemStacks, Text message, Text sender) {
		var itemStackList = DefaultedList.ofSize(itemStacks.size(), ItemStack.EMPTY);

		for (var index = 0; index < itemStacks.size(); index++) {
			itemStackList.set(index, itemStacks.get(index));
		}

		var packageItemStack = MailPackage.create(itemStackList, message, sender);
		return packageItemStack;
	}

	public static boolean deliverItemStackToPlayerMailbox(MinecraftServer server, PlayerEntity player, ItemStack itemStack) {
		var mailbox = getMailboxForPlayer(server, player);

		if (mailbox == null) {
			Commercialize.LOGGER.error("Could not deliver item stack '{}' to player '{}', no available delivery mailbox.",
					itemStack.getName(), player.getName());
			return false;
		}

		mailbox.service().sendMail(mailbox.getId(), itemStack);
		return true;
	}

	// Mailbox

	public static Mailbox getMailboxForPlayer(MinecraftServer server, PlayerEntity player) {
		var playerOwnedMailboxes = getMailboxesOwnedByPlayer(server, player);

		if (playerOwnedMailboxes.isEmpty()) {
			return null;
		}

		// If the player only owns one mailbox, always use it.

		if (playerOwnedMailboxes.size() == 1) {
			return playerOwnedMailboxes.get(0);
		}

		// If the player owns multiple mailboxes, find a marked one.

		for (var mailbox : playerOwnedMailboxes) {
			var mailboxName = mailbox.customName().toString();

			if (mailboxName.contains(Commercialize.CONFIG.mailboxMainMarker)) {
				return mailbox;
			}
		}

		// No conclusive pick was made, use first available mailbox for player as fallback.

		Commercialize.LOGGER.info(
				"Player '{}' does not have a determinable main mailbox for delivery ({} available). Picked first available as fallback.",
				player.getName(), playerOwnedMailboxes.size());

		return playerOwnedMailboxes.get(0);
	}

	private static List<Mailbox> getMailboxesOwnedByPlayer(MinecraftServer server, PlayerEntity player) {
		var playerId = player.getUuid();
		var mailboxesById = getMailboxesById(server);
		var playerOwnedMailboxes = mailboxesById.values().stream().filter(mailbox -> {
			var owner = mailbox.getOwner();

			if (owner.isEmpty()) {
				return false;
			}

			var ownerId = owner.get().getId();
			return ownerId.equals(playerId);
		}).toList();

		return playerOwnedMailboxes;
	}

	private static Map<UUID, Mailbox> getMailboxesById(MinecraftServer server) {
		var deliveryService = getDeliveryService(server);
		var accessibleDeliveryService = (DeliveryServiceAccessor) (Object) deliveryService;
		var mailboxes = accessibleDeliveryService.getMailboxes();

		return mailboxes;
	}

	private static DeliveryService getDeliveryService(MinecraftServer server) {
		var deliveryService = DeliveryService.get(server);
		return deliveryService.get();
	}

}
