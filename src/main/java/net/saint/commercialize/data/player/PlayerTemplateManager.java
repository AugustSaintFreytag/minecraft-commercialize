package net.saint.commercialize.data.player;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.random.Random;

public final class PlayerTemplateManager {

	// State

	private List<String> referencePlayerNames = new ArrayList<>();

	// Mock Names

	public void registerReferencePlayerNames(List<String> playerNames) {
		referencePlayerNames = playerNames;
	}

	public void clearReferencePlayerNames() {
		referencePlayerNames.clear();
	}

	public int numberOfReferencePlayerNames() {
		return referencePlayerNames.size();
	}

	public String randomReferencePlayerName(Random random) {
		return referencePlayerNames.get(random.nextInt(referencePlayerNames.size()));
	}

}
