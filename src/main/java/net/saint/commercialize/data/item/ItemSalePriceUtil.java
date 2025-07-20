package net.saint.commercialize.data.item;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.saint.commercialize.Commercialize;

public final class ItemSalePriceUtil {

	public static int totalSaleValueForItems(List<ItemStack> itemStacks) {
		var totalValue = 0;

		for (var itemStack : itemStacks) {
			if (itemStack.isEmpty()) {
				continue;
			}

			var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
			var itemBaseValue = Commercialize.ITEM_MANAGER.getValueForItem(itemIdentifier);

			if (itemBaseValue == 0) {
				continue;
			}

			var stackBaseValue = itemStack.getCount() * itemBaseValue;
			var stackSaleValue = stackBaseValue * Commercialize.CONFIG.sellingPriceFactor;

			totalValue += stackSaleValue;
		}

		return totalValue;
	}

}
