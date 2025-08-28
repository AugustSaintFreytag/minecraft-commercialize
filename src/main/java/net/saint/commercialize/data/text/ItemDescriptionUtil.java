package net.saint.commercialize.data.text;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class ItemDescriptionUtil {

	private static final String COUNT_SYMBOL = "x";

	public static String descriptionForItemStack(ItemStack stack) {
		var numberOfItems = stack.getCount();

		if (numberOfItems > 1) {
			return stack.getName().getString() + " (" + COUNT_SYMBOL + numberOfItems + ")";
		}

		return stack.getName().getString();
	}

	public static Text textForItemStack(ItemStack stack) {
		var numberOfItems = stack.getCount();

		if (numberOfItems > 1) {
			return Text.translatable(stack.getTranslationKey()).append(Text.literal(" (" + COUNT_SYMBOL + numberOfItems + ")"));
		}

		return Text.translatable(stack.getTranslationKey());
	}

}
