package net.saint.commercialize.data.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

/**
 * Client-side manager for player-related data.
 * 
 * Extendable to carry other profile data like player identifiers and names.
 */
public final class PlayerProfileManager {

	// References

	private MinecraftClient client;

	// State

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

}
