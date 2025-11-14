package net.saint.commercialize.data.item;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public final class ItemListUtil {

	public static DefaultedList<ItemStack> defauledItemStackListFromList(List<ItemStack> itemStacks) {
		var items = DefaultedList.ofSize(itemStacks.size(), ItemStack.EMPTY);

		for (int i = 0; i < itemStacks.size(); i++) {
			items.set(i, itemStacks.get(i));
		}

		return items;
	}

}
