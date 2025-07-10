package net.saint.commercialize.data.inventory;

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

public class PlayerInventoryCashUtil {

	// References

	private static final List<InventoryProvider> INVENTORY_PROVIDERS = new ArrayList<>();

	// Init

	public static void initialize() {
		INVENTORY_PROVIDERS.add(MainInventoryProvider.playerMainInventoryProvider());
	}

	// Reading & Counting

	public static int getCurrencyValueInAnyInventoriesForPlayer(PlayerEntity player) {
		var totalValue = 0;

		if (player == null) {
			return totalValue;
		}

		for (var provider : INVENTORY_PROVIDERS) {
			var inventory = provider.get(player);

			if (inventory == null) {
				continue;
			}

			totalValue += getCurrencyValueInInventory(inventory);
		}

		return totalValue;
	}

	private static int getCurrencyValueInInventory(Inventory inventory) {
		var totalValue = 0;

		for (var slot = 0; slot < inventory.size(); slot++) {
			var itemStack = inventory.getStack(slot);

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

	public static void addCurrencyToAnyInventoriesForPlayer(PlayerEntity player, int amount) {
		if (player == null || amount <= 0) {
			return;
		}

		for (var provider : INVENTORY_PROVIDERS) {
			var inventory = provider.get(player);

			if (inventory == null) {
				continue;
			}

			var remainingStacks = addCurrencyToInventory(inventory, amount);

			if (remainingStacks.isEmpty()) {
				break; // All currency added.
			}

			// Log remaining stacks that could not be added.
			Commercialize.LOGGER.info("Could not add all currency to inventory for player '{}'. Remaining stacks: {}.",
					player.getName().getString(), remainingStacks);
		}
	}

	public static List<ItemStack> addCurrencyToInventory(Inventory inventory, int amount) {
		var currencyStacks = getCurrencyStacksForAmount(amount);

		while (!currencyStacks.isEmpty()) {
			var itemStack = currencyStacks.remove(0);
			var firstFreeSlot = firstFreeSlotInInventory(inventory);

			if (firstFreeSlot == -1) {
				// No free slot found, stop adding.
				Commercialize.LOGGER.info("Could not find free slot in inventory for adding currency item stack '{}'.", itemStack);
				break;
			}

			inventory.setStack(firstFreeSlot, itemStack);
		}

		return currencyStacks;
	}

	private static List<ItemStack> getCurrencyStacksForAmount(int amount) {
		var stacks = new ArrayList<ItemStack>();

		for (var entry : Currency.CURRENCY_VALUES.entrySet()) {
			if (amount <= 0) {
				break;
			}

			var itemIdentifier = entry.getKey();
			var currencyDenomination = entry.getValue();

			var numberOfItems = Math.floorDiv(amount, currencyDenomination);

			if (numberOfItems <= 0) {
				continue;
			}

			var itemStack = new ItemStack(Registries.ITEM.get(itemIdentifier), (int) numberOfItems);
			stacks.add(itemStack);

			amount -= numberOfItems * currencyDenomination;
		}

		return stacks;
	}

	private static int firstFreeSlotInInventory(Inventory inventory) {
		for (var slot = 0; slot < inventory.size(); slot++) {
			if (inventory.getStack(slot).isEmpty()) {
				return slot;
			}
		}

		return -1;
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
