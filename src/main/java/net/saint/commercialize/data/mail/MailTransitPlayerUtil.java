package net.saint.commercialize.data.mail;

import java.util.List;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.commercialize.util.LocalizationUtil;

public final class MailTransitPlayerUtil {

	public static UUID playerIdFromItems(MinecraftServer server, List<MailTransitItem> items) {
		if (items.isEmpty()) {
			return null;
		}

		return items.get(0).recipient;
	}

	public static String playerNameForId(MinecraftServer server, UUID playerId) {
		var playerProfile = server.getUserCache().getByUuid(playerId);

		if (!playerProfile.isPresent()) {
			return LocalizationUtil.localizedString("text", "player_unknown");
		}

		return playerProfile.get().getName();
	}

	public static ServerPlayerEntity playerEntityForId(MinecraftServer server, UUID playerId) {
		if (playerId == null) {
			return null;
		}

		return server.getPlayerManager().getPlayer(playerId);
	}

}
