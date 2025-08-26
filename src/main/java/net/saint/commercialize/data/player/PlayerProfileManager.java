package net.saint.commercialize.data.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;

public final class PlayerProfileManager {

	// References

	private MinecraftClient client;

	// State

	private Map<UUID, GameProfile> playerProfileByPlayerId = new HashMap<>();
	private Map<String, UUID> playerIdByPlayerName = new HashMap<>();
	private Map<UUID, Identifier> textureIdByPlayerId = new HashMap<>();

	// Init

	public PlayerProfileManager(MinecraftClient client) {
		this.client = client;
	}

	// Skins

	public Identifier skinForPlayer(GameProfile profile) {
		var playerId = profile.getId();

		if (textureIdByPlayerId.containsKey(playerId)) {
			return textureIdByPlayerId.get(playerId);
		}

		loadAndCachePlayerSkin(profile);
		return client.getSkinProvider().loadSkin(profile);
	}

	public void loadAndCachePlayerSkin(GameProfile profile) {
		client.getSkinProvider().loadSkin(profile, (type, identifier, texture) -> {
			if (type != MinecraftProfileTexture.Type.SKIN) {
				return;
			}

			textureIdByPlayerId.put(profile.getId(), identifier);
		}, true);
	}

	// Profiles

	public GameProfile profileForPlayerId(UUID playerId) {
		if (playerProfileByPlayerId.containsKey(playerId)) {
			return playerProfileByPlayerId.get(playerId);
		}

		loadAndCacheProfileByPlayerId(playerId);
		return playerProfileByPlayerId.get(playerId);
	}

	public GameProfile profileForPlayerName(String playerName) {
		if (playerIdByPlayerName.containsKey(playerName)) {
			return profileForPlayerId(playerIdByPlayerName.get(playerName));
		}

		loadAndCacheProfileByPlayerName(playerName);
		var playerId = playerIdByPlayerName.get(playerName);

		return playerProfileByPlayerId.get(playerId);
	}

	private void loadAndCacheProfileByPlayerId(UUID playerId) {
		var networkHandler = client.getNetworkHandler();

		if (networkHandler == null) {
			Commercialize.LOGGER.error("Can not load and cache player profile '{}' from server-side, network handler not available.",
					playerId.toString());
			return;
		}

		var listEntry = networkHandler.getPlayerListEntry(playerId);
		cachePlayerProfile(listEntry);
	}

	private void loadAndCacheProfileByPlayerName(String playerName) {
		var networkHandler = client.getNetworkHandler();

		if (networkHandler == null) {
			Commercialize.LOGGER.error("Can not load and cache player profile '{}' from server-side, network handler not available.",
					playerName);
			return;
		}

		var listEntry = networkHandler.getPlayerListEntry(playerName);
		cachePlayerProfile(listEntry);
	}

	private void cachePlayerProfile(PlayerListEntry listEntry) {
		var profile = listEntry.getProfile();

		playerProfileByPlayerId.put(profile.getId(), profile);
		playerIdByPlayerName.put(profile.getName(), profile.getId());
	}

}
