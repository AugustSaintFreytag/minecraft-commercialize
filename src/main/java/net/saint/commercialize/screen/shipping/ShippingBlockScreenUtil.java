package net.saint.commercialize.screen.shipping;

import net.minecraft.text.Text;
import net.saint.commercialize.block.shipping.ShippingBlockInventory;
import net.saint.commercialize.data.item.ItemSalePriceUtil;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;

public final class ShippingBlockScreenUtil {

	public static Text textForSaleValueForInventory(ShippingBlockInventory inventory) {
		var totalSaleValue = totalSaleValueForItemsInInventory(inventory);
		var prefix = totalSaleValue > 0 ? "~ " : "";

		return Text.literal(prefix + CurrencyFormattingUtil.formatCurrency(totalSaleValue));
	}

	public static int totalSaleValueForItemsInInventory(ShippingBlockInventory inventory) {
		return ItemSalePriceUtil.totalSaleValueForItems(inventory.main.stacks);
	}

}
