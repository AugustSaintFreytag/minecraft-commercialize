package net.saint.commercialize.data.text;

import net.minecraft.item.ItemStack;

public final class ItemDescriptionUtil {

	public static String descriptionForItemStack(ItemStack stack) {
		var numberOfItems = stack.getCount();

		if (numberOfItems > 1) {
			return stack.getName().getString() + " (x" + numberOfItems + ")";
		}

		return stack.getName().getString();
	}

}
