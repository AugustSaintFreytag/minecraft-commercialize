package net.saint.commercialize.data.inventory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.function.TriFunction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.payment.Currency;

public class InventoryCashUtil {

	// Reading & Counting

	public static int getCurrencyValueInAnyInventoriesForPlayer(PlayerEntity player) {
		var totalValue = 0;

		if (player == null) {
			return totalValue;
		}

		for (var provider : InventoryAccessUtil.getInventoryProviders()) {
			var inventory = provider.get(player);

			if (inventory == null) {
				continue;
			}

			totalValue += getCurrencyValueInInventory(inventory);
		}

		return totalValue;
	}

	public static int getCurrencyValueInInventory(Inventory inventory) {
		var itemStacks = new ArrayList<ItemStack>();

		for (var slot = 0; slot < inventory.size(); slot++) {
			itemStacks.add(inventory.getStack(slot));
		}

		return getCurrencyValueInList(itemStacks);
	}

	public static int getCurrencyValueInList(List<ItemStack> itemStacks) {
		var totalValue = 0;

		for (var slot = 0; slot < itemStacks.size(); slot++) {
			var itemStack = itemStacks.get(slot);

			if (itemStack.isEmpty()) {
				continue;
			}

			var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
			var currencyValue = Currency.CURRENCY_VALUES.getOrDefault(itemIdentifier, 0);

			totalValue += currencyValue * itemStack.getCount();
		}

		return totalValue;
	}

	// Adding

	public static List<ItemStack> addCurrencyToInventory(Inventory inventory, int amount) {
		if (amount <= 0) {
			return List.of();
		}

		var remainingStacks = getCurrencyStacksForAmount(amount);

		if (inventory == null) {
			Commercialize.LOGGER.error("Can not add currency to null inventory.");
			return remainingStacks;
		}

		remainingStacks = addItemStacksToInventory(remainingStacks, inventory);

		return remainingStacks;
	}

	public static void addCurrencyToAnyInventoriesForPlayer(PlayerEntity player, int amount) {
		if (player == null || amount <= 0) {
			return;
		}

		var remainingStacks = getCurrencyStacksForAmount(amount);

		for (var provider : InventoryAccessUtil.getInventoryProviders()) {
			var inventory = provider.get(player);

			if (inventory == null) {
				continue;
			}

			remainingStacks = addItemStacksToInventory(remainingStacks, inventory);

			if (remainingStacks.isEmpty()) {
				break;
			}
		}

		while (!remainingStacks.isEmpty()) {
			// If not all currency items could be added, drop at player position.
			var itemStack = remainingStacks.remove(0);
			player.dropItem(itemStack, true);
		}
	}

	public static List<ItemStack> addItemStacksToInventory(List<ItemStack> itemStacks, Inventory inventory) {
		var remainingItemStacks = new ArrayDeque<ItemStack>(itemStacks);

		while (!remainingItemStacks.isEmpty()) {
			var itemStack = remainingItemStacks.poll();
			var leftoverItemStack = InventoryItemUtil.addStack(inventory, itemStack);

			if (leftoverItemStack.getCount() == itemStack.getCount()) {
				// Unable to add *any* of the stack's items to the inventory, presume full and bail.
				break;
			}

			if (!leftoverItemStack.isEmpty()) {
				// Items left in stack not yet added to inventory, push back to queue.
				remainingItemStacks.add(leftoverItemStack);
			}
		}

		return new ArrayList<ItemStack>(remainingItemStacks);
	}

	private static List<ItemStack> getCurrencyStacksForAmount(int amount) {
		var stacks = new ArrayList<ItemStack>();
		var remainingAmount = amount;

		for (var entry : Currency.ORDERED_CURRENCY_VALUES) {
			if (remainingAmount <= 0) {
				break;
			}

			var itemIdentifier = entry.getKey();
			var currencyDenomination = entry.getValue();

			var numberOfItems = Math.floorDiv(remainingAmount, currencyDenomination);
			var remainingNumberOfItems = numberOfItems;

			while (remainingNumberOfItems > 0) {
				var stackSize = Math.min(remainingNumberOfItems, 64);
				var itemStack = new ItemStack(Registries.ITEM.get(itemIdentifier), stackSize);
				remainingNumberOfItems -= stackSize;

				stacks.add(itemStack);
			}

			remainingAmount -= numberOfItems * currencyDenomination;
		}

		if (remainingAmount > 0) {
			Commercialize.LOGGER.error("Could not break down total amount {} into currency, {} left after distribution.", amount,
					remainingAmount);
		}

		return stacks;
	}

	// Removing

	/**
	 * Tries to pay the given amount in cash from the given inventory.
	 * 
	 * - If returned value is zero, the exact amount was paid. 
	 * - If returned value is positive, the inventory has insufficient funds to fully pay the amount.
	 * - If returned value is negative, the player is owed change from the transaction.
	 */
	public static int removeCurrencyFromInventory(Inventory inventory, int amount) {
		// This function assumes a preflight check if player has sufficient currency has been performed.
		// Approach: Go through slots in order, remove items as long as denomination fits, then move to next slot, through the inventory.
		// If amount can not be paid in sequence, pick smallest available coin and split into smaller denomination.

		var remainingAmount = new AtomicInteger(amount);

		// First Pass

		forEachCurrencyItemStackInInventory(inventory, (slot, itemStack, currencyDenomination) -> {
			if (remainingAmount.get() <= 0) {
				return null;
			}

			var numberOfItemsInStack = itemStack.getCount();
			var numberOfUseableItems = Math.min(numberOfItemsInStack, Math.floorDiv(remainingAmount.get(), currencyDenomination));

			itemStack.decrement(numberOfUseableItems);

			if (!itemStack.isEmpty()) {
				inventory.setStack(slot, itemStack);
			} else {
				inventory.removeStack(slot);
			}

			remainingAmount.getAndAdd(-(numberOfUseableItems * currencyDenomination));
			return null;
		});

		if (remainingAmount.get() == 0) {
			return 0;
		}

		// Second Pass

		// Find first stack of denomination higher than remaining amount.
		// Remove currency item from inventory.
		// Return owed change (removed item value minus remaining amount).

		forEachCurrencyItemStackInInventory(inventory, (slot, itemStack, currencyDenomination) -> {
			if (remainingAmount.get() <= 0) {
				return null;
			}

			if (currencyDenomination < remainingAmount.get()) {
				// Weird case because smaller denominations should've already been paid in first pass.
				return null;
			}

			// Found currency item that is worth more than remaining amount.
			itemStack.decrement(1);
			if (!itemStack.isEmpty()) {
				inventory.setStack(slot, itemStack);
			} else {
				inventory.removeStack(slot);
			}

			remainingAmount.getAndAdd(-currencyDenomination);

			return null;
		});

		return remainingAmount.get();
	}

	private static void forEachCurrencyItemStackInInventory(Inventory inventory, TriFunction<Integer, ItemStack, Integer, Void> consumer) {
		for (var slot = 0; slot < inventory.size(); slot++) {
			var itemStack = inventory.getStack(slot);

			if (itemStack.isEmpty()) {
				continue;
			}

			var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
			var currencyDenomination = Currency.CURRENCY_VALUES.getOrDefault(itemIdentifier, 0);

			if (currencyDenomination == 0) {
				continue;
			}

			consumer.apply(slot, itemStack, currencyDenomination);
		}
	}
}
