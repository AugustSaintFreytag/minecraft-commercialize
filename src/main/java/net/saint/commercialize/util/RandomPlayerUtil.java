package net.saint.commercialize.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.random.Random;
import net.saint.commercialize.Commercialize;

public final class RandomPlayerUtil {

	private static final int NUMBER_OF_SAMPLES = 3;
	private static final int MIN_NUMBER_OF_COMPONENTS = 1;
	private static final int MAX_NUMBER_OF_COMPONENTS = 3;
	private static final int MIN_NUMBER_OF_CHARACTERS = 4;
	private static final int MAX_NUMBER_OF_CHARACTERS = 12;

	public static String randomPlayerName(Random random) {
		// Gather components from a few sample player names
		var allNameComponents = new ArrayList<String>();
		for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
			var name = Commercialize.PLAYER_PROFILE_MANAGER.randomReferencePlayerName(random);
			allNameComponents.addAll(componentsFromName(name));
		}

		// Decide how many components to pick (as a guide)
		int targetComponents = random.nextBetween(MIN_NUMBER_OF_COMPONENTS, MAX_NUMBER_OF_COMPONENTS);
		var nameBuilder = new StringBuilder();

		// Pick up to targetComponents, but don't exceed MAX_NUMBER_OF_CHARACTERS
		for (int i = 0; i < targetComponents && !allNameComponents.isEmpty(); i++) {
			var comp = pickAndRemoveRandomNameComponentFromCollection(random, allNameComponents, i > 0);
			if (nameBuilder.length() + comp.length() > MAX_NUMBER_OF_CHARACTERS) {
				break;
			}
			nameBuilder.append(comp);
		}

		// If we're below the minimum length, grab more components until we hit the minimum
		while (nameBuilder.length() < MIN_NUMBER_OF_CHARACTERS && !allNameComponents.isEmpty()) {
			var comp = pickAndRemoveRandomNameComponentFromCollection(random, allNameComponents, true);
			if (nameBuilder.length() + comp.length() > MAX_NUMBER_OF_CHARACTERS) {
				break;
			}
			nameBuilder.append(comp);
		}

		// If still too short, pad with random lowercase letters
		while (nameBuilder.length() < MIN_NUMBER_OF_CHARACTERS) {
			char c = (char) ('a' + random.nextInt(26));
			nameBuilder.append(c);
		}

		// Finally, trim if we've somehow exceeded max
		if (nameBuilder.length() > MAX_NUMBER_OF_CHARACTERS) {
			return nameBuilder.substring(0, MAX_NUMBER_OF_CHARACTERS);
		}

		return nameBuilder.toString();
	}

	private static String pickAndRemoveRandomNameComponentFromCollection(Random random, List<String> collection, boolean allowNumerics) {
		if (collection.isEmpty()) {
			return "Elma";
		}

		var pickedIndex = random.nextInt(collection.size());
		var pickedNameComponent = collection.get(pickedIndex);

		if (!allowNumerics && pickedNameComponent.chars().allMatch(Character::isDigit)) {
			return pickAndRemoveRandomNameComponentFromCollection(random, collection, allowNumerics);
		}

		collection.remove(pickedIndex);
		return pickedNameComponent;
	}

	private static List<String> componentsFromName(String name) {
		// Example: "GrumpierCar6093" -> ["Grumpier", "Car", "6093"]
		// Example: "crybunnies" -> ["Crybunnies"]
		// Example: "_Anski" -> ["Anski"]

		var components = new ArrayList<String>();

		if (name == null || name.isEmpty()) {
			return components;
		}

		// Strip underscores in pre-pass
		var isCleaned = name.replaceAll("_+", "");
		var builder = new StringBuilder();

		for (var index = 0; index < isCleaned.length(); index++) {
			var character = isCleaned.charAt(index);

			if (builder.length() == 0) {
				builder.append(character);
				continue;
			}

			var last = builder.charAt(builder.length() - 1);
			var currentIsDigit = Character.isDigit(character);
			var lastIsDigit = Character.isDigit(last);
			var currentIsLetter = Character.isLetter(character);
			var lastIsLowercaseLetter = Character.isLowerCase(last);

			// Split on digit -> letter transitions
			if (currentIsDigit != lastIsDigit) {
				components.add(TextFormattingUtil.capitalize(builder.toString()));
				builder.setLength(0);
				builder.append(character);
			}

			// Split on lowercase -> uppercase
			else if (currentIsLetter && Character.isUpperCase(character) && lastIsLowercaseLetter) {
				components.add(TextFormattingUtil.capitalize(builder.toString()));
				builder.setLength(0);
				builder.append(character);
			} else {
				builder.append(character);
			}
		}

		if (builder.length() > 0) {
			components.add(TextFormattingUtil.capitalize(builder.toString()));
		}

		return components;
	}

}
