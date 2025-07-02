package net.saint.commercialize.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.random.Random;
import net.saint.commercialize.Commercialize;

public final class RandomPlayerUtil {

	private static final int NUMBER_OF_SAMPLES = 3;
	private static final int MIN_NUMBER_OF_COMPONENTS = 2;
	private static final int MAX_NUMBER_OF_COMPONENTS = 4;

	public static String randomPlayerName(Random random) {
		// Pick three random player name from manager cache.
		// Split names into lower/uppercase components and numbers.
		// Add all components into one set.
		// Pick 2-4 components from set (depending on size).
		// Combine into new name, apply basic formatting.

		var allNameComponents = new ArrayList<String>();

		for (var index = 0; index < NUMBER_OF_SAMPLES; index++) {
			var name = Commercialize.PLAYER_PROFILE_MANAGER.randomReferencePlayerName(random);
			allNameComponents.addAll(componentsFromName(name));
		}

		var pickedNameComponents = new ArrayList<String>();
		var numberOfPickedNameComponents = random.nextBetween(MIN_NUMBER_OF_COMPONENTS, MAX_NUMBER_OF_COMPONENTS);

		if (numberOfPickedNameComponents == 1) {
			// Remove numbers if only using one component
			allNameComponents.removeIf(component -> component.chars().allMatch(Character::isDigit));
		}

		for (var index = 0; index < numberOfPickedNameComponents; index++) {
			if (allNameComponents.isEmpty()) {
				break;
			}

			var pickedIndex = random.nextInt(allNameComponents.size());
			pickedNameComponents.add(allNameComponents.get(pickedIndex));
			allNameComponents.remove(pickedIndex);
		}

		var joinedNameComponents = String.join("", pickedNameComponents);
		return joinedNameComponents;
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
				components.add(capitalize(builder.toString()));
				builder.setLength(0);
				builder.append(character);
			}

			// Split on lowercase -> uppercase
			else if (currentIsLetter && Character.isUpperCase(character) && lastIsLowercaseLetter) {
				components.add(capitalize(builder.toString()));
				builder.setLength(0);
				builder.append(character);
			} else {
				builder.append(character);
			}
		}

		if (builder.length() > 0) {
			components.add(capitalize(builder.toString()));
		}

		return components;
	}

	private static String capitalize(String s) {
		if (s.isEmpty()) {
			return s;
		}

		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

}
