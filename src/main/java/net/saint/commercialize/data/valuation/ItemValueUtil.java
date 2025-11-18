package net.saint.commercialize.data.valuation;

import java.util.List;

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

	// Sale Value Approximation

	/**
	 * Returns the total approximate sale value for the provided list of item stacks.
	 * 
	 * Effectively returns the sum of all stacks multiplied by the selling price factor.
	 * Does not include server-side random price jitter applied during actual sales.
	 * 
	 * Intended for client-side presentation only.
	 */
	public static int getApproximateSaleValueForItems(List<ItemStack> itemStacks) {
		var totalValue = 0;

		for (var itemStack : itemStacks) {
			var stackValue = getValueForItemStack(itemStack);
			var stackSaleValue = stackValue * Commercialize.CONFIG.sellingPriceFactor;

			totalValue += stackSaleValue;
		}

		return totalValue;
	}

}
