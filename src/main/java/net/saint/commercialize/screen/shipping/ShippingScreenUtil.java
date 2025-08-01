package net.saint.commercialize.screen.shipping;

import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.shipping.ShippingBlockInventory;
import net.saint.commercialize.data.item.ItemSalePriceUtil;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;

public final class ShippingScreenUtil {

	// Sale

	public static Text textForSaleValueForInventory(ShippingBlockInventory inventory) {
		var totalSaleValue = totalSaleValueForItemsInInventory(inventory);
		var prefix = totalSaleValue > 0 ? "~ " : "";

		return Text.literal(prefix + CurrencyFormattingUtil.formatCurrency(totalSaleValue));
	}

	public static int totalSaleValueForItemsInInventory(ShippingBlockInventory inventory) {
		return ItemSalePriceUtil.totalSaleValueForItems(inventory.getItemStacks());
	}

	// Shipping

	public static Text textForNextShippingTime(World world) {
		var shippingInterval = Commercialize.CONFIG.shippingExchangeInterval;
		var ticksToNextSale = shippingInterval - world.getTime() % shippingInterval;
		var formattedTime = TimeFormattingUtil.formattedTime(ticksToNextSale);

		return Text.of(formattedTime);
	}

}
