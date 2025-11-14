package net.saint.commercialize.data.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public final class InventoryItemUtil {

	// Configuration

	private static final int ITEM_STACK_ANIMATION_TIME = 5;

	// Stack Movement

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

	// Slot Movement

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

	// Transformation

	public static List<ItemStack> itemStackListFromInventory(Inventory inventory) {
		var itemStacks = new ArrayList<ItemStack>();

		for (int i = 0; i < inventory.size(); i++) {
			var itemStack = inventory.getStack(i);

			if (itemStack.isEmpty()) {
				continue;
			}

			itemStacks.add(itemStack);
		}

		return itemStacks;
	}

	public static DefaultedList<ItemStack> defaultedItemStackListFromInventory(Inventory inventory) {
		var itemStacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

		for (int i = 0; i < inventory.size(); i++) {
			var itemStack = inventory.getStack(i);
			itemStacks.set(i, itemStack);
		}

		return itemStacks;
	}

	// Iteration

	public static void forEachItemStackInInventory(Inventory inventory, BiConsumer<ItemStack, Integer> consumer) {
		for (int i = 0; i < inventory.size(); i++) {
			var itemStack = inventory.getStack(i);

			if (itemStack.isEmpty()) {
				continue;
			}

			consumer.accept(itemStack, i);
		}
	}

}
