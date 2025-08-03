package net.saint.commercialize.data.mail;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.text.TimeFormattingUtil;
import net.saint.commercialize.util.LocalizationUtil;

public final class MailTransitNotificationUtil {

	// Delivery Attempt (Execute)

	public static void notifyPlayersOfFailedDeliveryAttempts(MinecraftServer server, Map<UUID, List<MailTransitItem>> itemsByPlayer) {
		for (var playerId : itemsByPlayer.keySet()) {
			notifyPlayerOfFailedDeliveryAttempt(server, itemsByPlayer.get(playerId));
		}
	}

	public static void notifyPlayerOfFailedDeliveryAttempt(MinecraftServer server, List<MailTransitItem> items) {
		var message = deliveryAttemptMessageForPlayerAndItems(server, items);
		var playerId = MailTransitPlayerUtil.playerIdFromItems(server, items);
		var playerName = MailTransitPlayerUtil.playerNameForId(server, playerId);
		var player = MailTransitPlayerUtil.playerEntityForId(server, playerId);

		if (player == null) {
			// Player is offline, can not message.
			Commercialize.LOGGER.warn("Can not notify offline player '{}' ({}) for failed delivery attempts.", playerName, playerId);
			return;
		}

		// #ffaaaaff
		player.sendMessage(Text.of(message).copy().setStyle(Style.EMPTY.withColor(0xffaaaa).withItalic(true)));
	}

	// Delivery Attempt (Message)

	private static String deliveryAttemptMessageForPlayerAndItems(MinecraftServer server, List<MailTransitItem> items) {
		var time = server.getOverworld().getTime();
		var numberOfPackages = items.size();
		var numberOfPackagesAboutToExpire = numberOfItemsAboutToExpire(items);
		var timeUntilNextAttempt = Commercialize.CONFIG.mailDeliveryTime - time % Commercialize.CONFIG.mailDeliveryTime;
		var formattedTimeUntilNextAttempt = "~" + TimeFormattingUtil.formattedTime(timeUntilNextAttempt);

		if (numberOfPackagesAboutToExpire > 1) {
			return LocalizationUtil.localizedString("text", "delivery.expiration_notice_last", numberOfPackages,
					formattedTimeUntilNextAttempt, numberOfPackagesAboutToExpire);
		}

		return LocalizationUtil.localizedString("text", "delivery.expiration_notice", numberOfPackages, formattedTimeUntilNextAttempt);
	}

	private static int numberOfItemsAboutToExpire(List<MailTransitItem> items) {
		return items.stream().filter(item -> {
			return item.numberOfDeliveryAttempts >= Commercialize.CONFIG.maxNumberOfDeliveryAttempts;
		}).toList().size();
	}

}
