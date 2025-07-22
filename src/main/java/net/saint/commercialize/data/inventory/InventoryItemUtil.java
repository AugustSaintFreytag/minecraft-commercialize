package net.saint.commercialize.data.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public final class InventoryItemUtil {

	// Configuration

	private static final int ITEM_STACK_ANIMATION_TIME = 5;

	// Logic

	public static ItemStack addStack(Inventory inventory, ItemStack stack) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		var itemStack = stack.copy();
		addToExistingSlot(inventory, itemStack);

		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		addToNewSlot(inventory, itemStack);
		return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
	}

	public static ItemStack getStack(Inventory inventory, int slot) {
		if (slot < 0 || slot >= inventory.size()) {
			return ItemStack.EMPTY;
		}

		return (ItemStack) inventory.getStack(slot);
	}

	public static void setStack(Inventory inventory, int slot, ItemStack stack) {
		inventory.setStack(slot, stack);

		if (!stack.isEmpty() && stack.getCount() > inventory.getMaxCountPerStack()) {
			stack.setCount(inventory.getMaxCountPerStack());
		}

		stack.setBobbingAnimationTime(ITEM_STACK_ANIMATION_TIME);
		inventory.markDirty();
	}

	private static void addToNewSlot(Inventory inventory, ItemStack stack) {
		for (int i = 0; i < inventory.size(); ++i) {
			var itemStack = getStack(inventory, i);

			if (!itemStack.isEmpty() || !inventory.isValid(i, stack)) {
				continue;
			}

			setStack(inventory, i, stack.copyAndEmpty());
			var modifiedStack = inventory.getStack(i);
			modifiedStack.setBobbingAnimationTime(ITEM_STACK_ANIMATION_TIME);

			inventory.markDirty();
		}
	}

	private static void addToExistingSlot(Inventory inventory, ItemStack stack) {
		for (int i = 0; i < inventory.size(); ++i) {
			var itemStack = inventory.getStack(i);

			if (ItemStack.canCombine(itemStack, stack)) {
				transfer(inventory, stack, itemStack);

				if (stack.isEmpty()) {
					return;
				}
			}
		}
	}

	private static void transfer(Inventory inventory, ItemStack source, ItemStack target) {
		var i = Math.min(inventory.getMaxCountPerStack(), target.getMaxCount());
		var j = Math.min(source.getCount(), i - target.getCount());

		if (j > 0) {
			target.increment(j);
			target.setBobbingAnimationTime(ITEM_STACK_ANIMATION_TIME);

			source.decrement(j);
			source.setBobbingAnimationTime(ITEM_STACK_ANIMATION_TIME);

			inventory.markDirty();
		}
	}

}
