package net.saint.commercialize.data.item;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Identifier;

public final class ItemManager {

	// Properties

	private Map<Identifier, Integer> valueByItem = new HashMap<>();

	// Access

	public int size() {
		return valueByItem.size();
	}

	public int getValueForItem(Identifier item) {
		return valueByItem.getOrDefault(item, 0);
	}

	public Map<Identifier, Integer> getValuesByItem() {
		return new HashMap<Identifier, Integer>() {
			{
				this.putAll(valueByItem);
			}
		};
	}

	// Mutation

	public void registerItemValue(Identifier item, int value) {
		valueByItem.put(item, value);
	}

	public void clearItemValues() {
		valueByItem.clear();
	}

}
