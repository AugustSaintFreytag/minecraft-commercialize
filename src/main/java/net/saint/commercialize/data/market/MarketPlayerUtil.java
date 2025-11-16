package net.saint.commercialize.data.market;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.commercialize.data.mail.MailTransitItem;
import net.saint.commercialize.data.player.PlayerProfileAccessUtil;
import net.saint.commercialize.util.LocalizationUtil;

public final class MarketPlayerUtil {

	// Player Validation

	public static boolean isKnownPlayerId(MinecraftServer server, UUID playerId) {
		return getPlayerProfileForId(server, playerId) != null;
	}

	// Player Name

	public static String getPlayerNameForId(MinecraftServer server, UUID playerId) {
		var playerProfile = PlayerProfileAccessUtil.getPlayerProfileById(server, playerId);

		if (playerProfile == null) {
			return LocalizationUtil.localizedString("text", "player_unknown");
		}

		return playerProfile.getName();
	}

	// Player Id

	public static ServerPlayerEntity getPlayerEntityForId(MinecraftServer server, UUID playerId) {
		if (playerId == null) {
			return null;
		}

		return server.getPlayerManager().getPlayer(playerId);
	}

	// Player Profile

	public static GameProfile getPlayerProfileForId(MinecraftServer server, UUID playerId) {
		if (playerId == null) {
			return null;
		}

		return PlayerProfileAccessUtil.getPlayerProfileById(server, playerId);
	}

	// Recipient

	public static UUID getPlayerIdAsRecipientFromItems(MinecraftServer server, List<MailTransitItem> items) {
		if (items.isEmpty()) {
			return null;
		}

		return items.get(0).recipient;
	}

}
