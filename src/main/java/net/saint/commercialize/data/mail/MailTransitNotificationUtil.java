package net.saint.commercialize.data.mail;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.market.MarketPlayerUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;
import net.saint.commercialize.util.LocalizationUtil;

public final class MailTransitNotificationUtil {

	public record PlayerParameters(UUID playerId, String playerName, ServerPlayerEntity player) {
	}

	// Delivery Attempt (Execute)

	public static void notifyPlayersOfFailedDeliveryAttempts(MinecraftServer server, Map<UUID, List<MailTransitItem>> itemsByPlayer) {
		for (var playerId : itemsByPlayer.keySet()) {
			notifyPlayerOfFailedDeliveryAttempt(server, itemsByPlayer.get(playerId));
		}
	}

	public static void notifyPlayerOfFailedDeliveryAttempt(MinecraftServer server, List<MailTransitItem> items) {
		withOnlinePlayerParametersForItems(server, items, parameters -> {
			// Text color: #ffaaaaff

			var message = deliveryAttemptMessageForPlayerAndItems(server, items);
			parameters.player.sendMessage(Text.of(message).copy().setStyle(Style.EMPTY.withColor(0xffaaaa).withItalic(true)));
		});
	}

	// Delivery Attempt (Message)

	private static String deliveryAttemptMessageForPlayerAndItems(MinecraftServer server, List<MailTransitItem> items) {
		var time = server.getOverworld().getTimeOfDay();
		var numberOfPackages = items.size();
		var numberOfPackagesAboutToExpire = numberOfItemsAboutToExpire(items);
		var numberOfDeliveryAttempts = maxNumberOfPreviousDeliveryAttempts(items);
		var timeUntilNextAttempt = Commercialize.CONFIG.mailDeliveryTime - time % Commercialize.CONFIG.mailDeliveryTime;
		var formattedTimeUntilNextAttempt = "~" + TimeFormattingUtil.formattedTime(timeUntilNextAttempt);

		if (numberOfPackagesAboutToExpire > 0) {
			return LocalizationUtil.localizedString("text", "delivery.expiration_notice_last", numberOfPackages,
					formattedTimeUntilNextAttempt, numberOfPackagesAboutToExpire, numberOfPackages, numberOfDeliveryAttempts,
					Commercialize.CONFIG.maxNumberOfDeliveryAttempts);
		}

		return LocalizationUtil.localizedString("text", "delivery.expiration_notice", numberOfPackages, formattedTimeUntilNextAttempt,
				numberOfDeliveryAttempts, Commercialize.CONFIG.maxNumberOfDeliveryAttempts);
	}

	private static int numberOfItemsAboutToExpire(List<MailTransitItem> items) {
		return (int) items.stream().filter(item -> item.numberOfDeliveryAttempts >= Commercialize.CONFIG.maxNumberOfDeliveryAttempts)
				.count();
	}

	private static int maxNumberOfPreviousDeliveryAttempts(List<MailTransitItem> items) {
		return items.stream().mapToInt(item -> item.numberOfDeliveryAttempts).max().orElse(0);
	}

	// Delivery Discard (Execute)

	public static void notifyPlayersOfDeliveryDiscard(MinecraftServer server, Map<UUID, List<MailTransitItem>> itemsByPlayer) {
		for (var playerId : itemsByPlayer.keySet()) {
			notifyPlayerOfDeliveryDiscard(server, itemsByPlayer.get(playerId));
		}
	}

	public static void notifyPlayerOfDeliveryDiscard(MinecraftServer server, List<MailTransitItem> items) {
		withOnlinePlayerParametersForItems(server, items, parameters -> {
			// Text color: #ff7e7eff

			var message = deliveryDiscardMessageForPlayerAndItems(server, items);
			parameters.player.sendMessage(Text.of(message).copy().setStyle(Style.EMPTY.withColor(0xff7e7e).withItalic(true)));
		});
	}

	private static String deliveryDiscardMessageForPlayerAndItems(MinecraftServer server, List<MailTransitItem> items) {
		var numberOfPackages = items.size();
		return LocalizationUtil.localizedString("text", "delivery.discard_notice", numberOfPackages);
	}

	// Utility

	private static void withOnlinePlayerParametersForItems(MinecraftServer server, List<MailTransitItem> items,
			Consumer<PlayerParameters> block) {
		var playerId = MarketPlayerUtil.playerIdFromItems(server, items);
		var playerName = MarketPlayerUtil.playerNameForId(server, playerId);
		var player = MarketPlayerUtil.playerEntityForId(server, playerId);

		if (player == null) {
			// Player is offline, can not message.
			Commercialize.LOGGER.warn("Can not notify offline player '{}' ({}) for failed delivery attempts.", playerName, playerId);
			return;
		}

		var parameters = new PlayerParameters(playerId, playerName, player);
		block.accept(parameters);
	}

}
