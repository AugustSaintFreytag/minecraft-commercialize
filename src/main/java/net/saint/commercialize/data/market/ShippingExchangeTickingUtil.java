package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.ithundxr.createnumismatics.content.bank.CardItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.shipping.ShippingBlockEntity;
import net.saint.commercialize.block.shipping.ShippingBlockInventory;
import net.saint.commercialize.data.bank.BankAccountAccessUtil;
import net.saint.commercialize.data.inventory.InventoryCashUtil;

public final class ShippingExchangeTickingUtil {

	// Library

	public record ShippingRemovalResult(List<ItemStack> items, int totalValue) {
	}

	public enum ShippingTickResult {
		SOLD, NO_ITEMS, FAILURE
	}

	// Logic

	public static void tickShippingIfNecessary(World world, ShippingBlockEntity blockEntity, Consumer<ShippingTickResult> callback) {
		var time = world.getTimeOfDay();

		if (time % Commercialize.CONFIG.shippingExchangeInterval == 0) {
			tickShipping(world, blockEntity, callback);
		}
	}

	public static void tickShipping(World world, ShippingBlockEntity blockEntity, Consumer<ShippingTickResult> callback) {
		// Gather and remove sellable item stacks from inventory.
		// Determine total sale value for player, apply optional jitter to sale price.
		// Pay out owed value to player in cash or account balance depending on card.

		var inventory = blockEntity.inventory;
		var result = removeItemsForShippingFromInventory(world, inventory, false);
		var itemValue = result.totalValue;

		if (itemValue == 0) {
			callback.accept(ShippingTickResult.NO_ITEMS);
			return;
		}

		var saleValue = (int) (itemValue * Commercialize.CONFIG.sellingPriceFactor * randomSellingPriceJitterFactor(world));
		var paymentCard = blockEntity.inventory.getCardStack();
		var playerHasPaymentCard = !paymentCard.isEmpty();

		// Deposit to Account

		if (playerHasPaymentCard) {
			var boundAccountId = CardItem.get(paymentCard);

			if (boundAccountId == null) {
				Commercialize.LOGGER.warn("Could not resolve account id from provided payment card.");
				callback.accept(ShippingTickResult.FAILURE);
				return;
			}

			BankAccountAccessUtil.depositAccountBalanceForCard(paymentCard, saleValue);
			removeItemsForShippingFromInventory(world, inventory, true);

			callback.accept(ShippingTickResult.SOLD);
			return;
		}

		// Deposit in Cash

		removeItemsForShippingFromInventory(world, inventory, true);
		var remainingCurrencyItems = InventoryCashUtil.addCurrencyToInventory(inventory.main, saleValue);
		dropItemStacksInWorld(world, blockEntity.getPos(), remainingCurrencyItems);

		callback.accept(ShippingTickResult.SOLD);
	}

	private static void dropItemStacksInWorld(World world, BlockPos position, List<ItemStack> itemStacks) {
		var itemList = itemListFromItemStacks(itemStacks);
		ItemScatterer.spawn(world, position, itemList);
	}

	private static DefaultedList<ItemStack> itemListFromItemStacks(List<ItemStack> itemStacks) {
		var items = DefaultedList.ofSize(itemStacks.size(), ItemStack.EMPTY);

		for (int i = 0; i < itemStacks.size(); i++) {
			items.set(i, itemStacks.get(i));
		}

		return items;
	}

	private static double randomSellingPriceJitterFactor(World world) {
		return 1 + world.getRandom().nextTriangular(0, 0.05);
	}

	public static ShippingRemovalResult removeItemsForShippingFromInventory(World world, ShippingBlockInventory inventory,
			boolean shouldRemove) {
		var pendingItems = new ArrayList<ItemStack>();
		var totalValue = 0;

		for (var slot : ShippingBlockInventory.MAIN_SLOTS) {
			var itemStack = inventory.getStack(slot);
			var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
			var itemValue = Commercialize.ITEM_MANAGER.getValueForItem(itemIdentifier);

			// Item has no value and can not be sold.
			if (itemValue == 0) {
				continue;
			}

			// Item has value and will be sold.
			pendingItems.add(itemStack);
			totalValue += itemValue;

			// Item stack can be removed from inventory.
			if (shouldRemove) {
				inventory.removeStack(slot);
				inventory.markDirty();
			}
		}

		return new ShippingRemovalResult(pendingItems, totalValue);
	}

}
