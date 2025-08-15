package net.saint.commercialize.screen.selling;

import net.minecraft.item.ItemStack;

public class SellingScreenState {

	public ItemStack selectedItem = ItemStack.EMPTY;

	public int offerPrice = 0;

	public long offerDuration = 0;

	public SellingPostStrategy offerPostStrategy = SellingPostStrategy.AS_STACK;

}
