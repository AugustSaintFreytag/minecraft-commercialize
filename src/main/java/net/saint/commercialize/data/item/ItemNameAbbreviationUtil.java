package net.saint.commercialize.data.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemNameAbbreviationUtil {

	private static Map<String, String> abbreviatedNameComponents = new HashMap<>() {
		{
			this.put("Redstone", "Redst.");
			this.put("Glowstone", "Glowst.");
			this.put("Ingot", "Ing.");
			this.put("Powder", "Pwdr.");
			this.put("Diamond", "Dmnd.");
			this.put("Netherite", "Nthrt.");
			this.put("Book", "Bk.");
			this.put("Enchantment", "Ench.");
			this.put("Enchanting", "Ench.");
		}
	};

	public static Text abbreviatedItemText(ItemStack stack, int limit) {
		var itemNameString = I18n.translate(stack.getTranslationKey());
		var abbreviatedItemNameString = abbreviatedItemName(itemNameString, limit);

		return Text.of(abbreviatedItemNameString);
	}

	public static String abbreviatedItemName(String name, int limit) {
		// Split name on space into name components.
		// If entire name fits in character limit, return as is.
		// If name is above limit, abbreviate longest components first.
		// Abbreviate until name fits in limit.
		// If name does not fit limit after all componets are abbreviated, ellipsize.

		if (name.length() <= limit) {
			return name;
		}

		var components = new ArrayList<String>(java.util.List.of(name.split(" ")));
		var currentName = name;

		while (currentName.length() > limit) {
			var longestIndex = -1;
			var longestLength = 0;

			for (var i = 0; i < components.size(); i++) {
				var component = components.get(i);
				if (component.length() > longestLength && abbreviatedNameComponents.containsKey(component)) {
					longestLength = component.length();
					longestIndex = i;
				}
			}

			if (longestIndex < 0) {
				break;
			}

			var originalComponent = components.get(longestIndex);
			var abbreviation = abbreviatedNameComponents.get(originalComponent);

			components.set(longestIndex, abbreviation);
			currentName = String.join(" ", components);
		}

		if (currentName.length() > limit) {
			if (limit > 3) {
				var truncated = currentName.substring(0, limit - 3);

				if (truncated.endsWith(".")) {
					return truncated;
				}

				return truncated + "...";
			} else {
				return currentName.substring(0, limit);
			}
		}

		return currentName;
	}

}
