package net.saint.commercialize.data.player;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;

/**
 * Server-side access to player profiles.
 */
public final class PlayerProfileAccessUtil {

	// Properties

	private static String FALLBACK_PLAYER_NAME = "(Unknown)";

	// Profile Access

	/**
	 * Server-side access to player profiles by first checking online players, then checking cached profiles.
	 */
	public static GameProfile getPlayerProfileById(MinecraftServer server, UUID playerId) {
		var onlineProfile = getOnlinePlayerProfileById(server, playerId);

		if (onlineProfile != null) {
			return onlineProfile;
		}

		return getCachedPlayerProfileById(server, playerId);
	}

	/**
	 * Server-side access to cached player profiles that have previously connected to the server.
	 */
	public static GameProfile getCachedPlayerProfileById(MinecraftServer server, UUID playerId) {
		var userCache = server.getUserCache();
		var gameProfile = userCache.getByUuid(playerId);

		if (gameProfile.isEmpty()) {
			return null;
		}

		return gameProfile.get();
	}

	/**
	 * Server-side access to player profiles that are currently connected to the server.
	 */
	public static GameProfile getOnlinePlayerProfileById(MinecraftServer server, UUID playerId) {
		var player = server.getPlayerManager().getPlayer(playerId);

		if (player == null) {
			return null;
		}

		return player.getGameProfile();
	}

	// Name Access

	public static String getPlayerNameById(MinecraftServer server, UUID playerId) {
		var profile = getPlayerProfileById(server, playerId);

		if (profile == null) {
			return FALLBACK_PLAYER_NAME;
		}

		return profile.getName();
	}

}
