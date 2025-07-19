package net.saint.commercialize.block.shipping;

import static net.saint.commercialize.util.Values.ifPresent;
import static net.saint.commercialize.util.Values.returnIfPresent;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class ShippingBlockInventory implements Inventory {

	// Library

	private record InventorySlot(SimpleInventory inventory, int slot) {
	}

	// Configuration

	public static final int[] MAIN_SLOTS = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	public static final int CARD_SLOT = 9;

	// Properties

	public final SimpleInventory main = new SimpleInventory(9);
	public final SimpleInventory card = new SimpleInventory(1);

	private final List<SimpleInventory> allInventories = ImmutableList.of(this.main, this.card);

	private List<InventoryChangedListener> listeners;

	// Init

	public ShippingBlockInventory() {
		this.main.addListener(sender -> {
			this.markDirty();
		});

		this.card.addListener(sender -> {
			this.markDirty();
		});
	}

	// Access

	@Override
	public int size() {
		return main.size() + card.size();
	}

	@Override
	public boolean isEmpty() {
		for (var inventory : allInventories) {
			if (!inventory.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void clear() {
		for (var inventory : allInventories) {
			inventory.clear();
		}
	}

	// Stack Access

	public DefaultedList<ItemStack> getStacks() {
		var allStacks = DefaultedList.ofSize(size(), ItemStack.EMPTY);

		for (int i = 0; i < main.size(); i++) {
			allStacks.set(i, main.getStack(i));
		}

		for (int i = 0; i < card.size(); i++) {
			allStacks.set(i + main.size(), card.getStack(i));
		}

		return allStacks;
	}

	@Override
	public ItemStack getStack(int slot) {
		return returnIfPresent(getInventoryAndSlot(slot), inventorySlot -> {
			return inventorySlot.inventory.getStack(inventorySlot.slot);
		}, ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		return returnIfPresent(getInventoryAndSlot(slot), inventorySlot -> {
			return inventorySlot.inventory.removeStack(inventorySlot.slot, amount);
		}, ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int slot) {
		return returnIfPresent(getInventoryAndSlot(slot), inventorySlot -> {
			return inventorySlot.inventory.removeStack(inventorySlot.slot);
		}, ItemStack.EMPTY);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		ifPresent(getInventoryAndSlot(slot), inventorySlot -> {
			inventorySlot.inventory.setStack(inventorySlot.slot, stack);
		});
	}

	// Listeners

	public void addListener(InventoryChangedListener listener) {
		if (this.listeners == null) {
			this.listeners = Lists.<InventoryChangedListener>newArrayList();
		}

		this.listeners.add(listener);
	}

	public void removeListener(InventoryChangedListener listener) {
		if (this.listeners == null) {
			return;
		}

		this.listeners.remove(listener);
	}

	// Sync

	@Override
	public void markDirty() {
		if (this.listeners == null) {
			return;
		}

		for (InventoryChangedListener inventoryChangedListener : this.listeners) {
			inventoryChangedListener.onInventoryChanged(this);
		}
	}

	// Player

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	// Description

	@Override
	public String toString() {
		return ((List<ItemStack>) this.getStacks().stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList())).toString();
	}

	// Utility

	private InventorySlot getInventoryAndSlot(int slot) {
		if (slot < 0 || slot >= size()) {
			return null;
		}

		for (SimpleInventory inventory : allInventories) {
			if (slot < inventory.size()) {
				return new InventorySlot(inventory, slot);
			}

			slot -= inventory.size();
		}

		return null;
	}

}
