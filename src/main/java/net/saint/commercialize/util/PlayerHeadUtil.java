package net.saint.commercialize.util;

import com.google.common.hash.Hashing;

import net.minecraft.util.math.random.Random;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.screen.market.MarketScreenAssets;

public final class PlayerHeadUtil {

	// Properties

	private static final int NUMBER_OF_ROWS = 8;
	private static final int TEXTURES_PER_ROW = 32;

	// Access

	public static int numberOfHeads() {
		return NUMBER_OF_ROWS * TEXTURES_PER_ROW;
	}

	// Random

	public static int randomPlayerHeadIndex(Random random) {
		return random.nextInt(numberOfHeads());
	}

	public static TextureReference randomPlayerHeadTexture(Random random) {
		var index = randomPlayerHeadIndex(random);
		var row = index / TEXTURES_PER_ROW;
		var column = index % TEXTURES_PER_ROW;

		return new TextureReference(MarketScreenAssets.PLAYER_HEADS, column * 8, row * 8, 8, 8);
	}

	// Random from Name

	public static int playerHeadIndexForName(String name) {
		var nameHash = hashValue(name);
		var random = Random.create(nameHash);

		return randomPlayerHeadIndex(random);
	}

	public static TextureReference playerHeadTextureForName(String name) {
		var index = playerHeadIndexForName(name);
		var row = index / TEXTURES_PER_ROW;
		var column = index % TEXTURES_PER_ROW;

		return new TextureReference(MarketScreenAssets.PLAYER_HEADS, column * 8, row * 8, 8, 8);
	}

	// Utility

	private static long hashValue(String string) {
		return Hashing.murmur3_128().hashUnencodedChars(string).asLong();
	}

}
