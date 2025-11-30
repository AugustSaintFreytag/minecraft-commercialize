package net.saint.commercialize.data.text;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class ItemDescriptionUtil {

	private static final String COUNT_SYMBOL = "x";

	public static String descriptionForItemStackWithCount(ItemStack stack) {
		var numberOfItems = stack.getCount();

		if (numberOfItems > 1) {
			return stack.getName().getString() + " (" + COUNT_SYMBOL + numberOfItems + ")";
		}

		return stack.getName().getString();
	}

	public static Text textForItemStackWithCount(ItemStack stack) {
		var itemStackText = Text.translatable(stack.getTranslationKey());
		var numberOfItems = stack.getCount();

		if (numberOfItems == 1) {
			return itemStackText;
		}

		return itemStackText.append(textForItemStackCount(numberOfItems));
	}

	public static Text textForItemStackCount(int count) {
		return Text.literal(" (" + COUNT_SYMBOL + count + ")");
	}

}
