package net.saint.commercialize.data.valuation;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;

public final class ItemSaleValueUtil {

	// Real

	public static int getSaleValueForValue(World world, int value) {
		return (int) (value * Commercialize.CONFIG.sellingPriceFactor * randomSellingPriceJitterFactor(world));
	}

	private static double randomSellingPriceJitterFactor(World world) {
		return 1 + world.getRandom().nextTriangular(0, 0.05);
	}

	// Approximation

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
			var stackValue = ItemValueUtil.getValueForItemStack(itemStack);
			var stackSaleValue = stackValue * Commercialize.CONFIG.sellingPriceFactor;

			totalValue += stackSaleValue;
		}

		return totalValue;
	}

}
