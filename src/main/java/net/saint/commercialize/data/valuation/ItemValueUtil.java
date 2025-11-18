package net.saint.commercialize.data.valuation;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.saint.commercialize.Commercialize;

public final class ItemValueUtil {

	// Value Calculation

	/**
	 * Returns the total value for the provided item stack.
	 */
	public static int getValueForItemStack(ItemStack itemStack) {
		var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
		var itemValue = Commercialize.ITEM_MANAGER.getValueForItem(itemIdentifier);
		var stackValue = itemValue * itemStack.getCount();

		return stackValue;
	}

}
