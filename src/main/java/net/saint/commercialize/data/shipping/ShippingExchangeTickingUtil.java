package net.saint.commercialize.data.shipping;

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
			var boundBankAccount = BankAccountAccessUtil.getBankAccountForCard(paymentCard);

			if (boundBankAccount == null) {
				Commercialize.LOGGER.warn("Could not get bank account from provided payment card with id '{}' in shipping block.",
						boundAccountId);
				callback.accept(ShippingTickResult.FAILURE);
				return;
			}

			Commercialize.LOGGER.info("Depositing {} ¤ to bank account '{}' ({}) from provided payment card in shipping block.", saleValue,
					boundBankAccount.getLabel(), boundAccountId);

			var preDepositBalance = boundBankAccount.getBalance();
			BankAccountAccessUtil.depositAccountBalanceForCard(paymentCard, saleValue);
			var postDepositBalance = boundBankAccount.getBalance();

			if (preDepositBalance + saleValue != postDepositBalance) {
				Commercialize.LOGGER.error(
						"Bank account '{}' ({}) balance did not update correctly after depositing {} ¤ from shipping block payment card. "
								+ "Pre-deposit balance: {} ¤, post-deposit balance: {} ¤.",
						boundBankAccount.getLabel(), boundAccountId, saleValue, preDepositBalance, postDepositBalance);
				callback.accept(ShippingTickResult.FAILURE);
				return;
			}

			Commercialize.LOGGER.info(
					"Sold {} item(s) from shipping block, deposited {} ¤ to bank account '{}' ({}) from shipping block payment card.",
					result.items.size(), saleValue, boundBankAccount.getLabel(), boundAccountId);

			removeItemsForShippingFromInventory(world, inventory, true);

			callback.accept(ShippingTickResult.SOLD);
			return;
		}

		// Deposit in Cash

		removeItemsForShippingFromInventory(world, inventory, true);
		var remainingCurrencyItems = InventoryCashUtil.addCurrencyToInventory(inventory.main, saleValue);
		dropItemStacksInWorld(world, blockEntity.getPos(), remainingCurrencyItems);

		Commercialize.LOGGER.info(
				"Sold {} item(s) from shipping block, paid out {} ¤ in cash ({} ¤ dropped in world due to full inventory).",
				result.items.size(), saleValue, InventoryCashUtil.getCurrencyValueInList(remainingCurrencyItems));

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
			var stackValue = itemValue * itemStack.getCount();

			// Item has no value and can not be sold.
			if (stackValue == 0) {
				continue;
			}

			// Item has value and will be sold.
			pendingItems.add(itemStack);
			totalValue += stackValue;

			// Item stack can be removed from inventory.
			if (shouldRemove) {
				inventory.removeStack(slot);
				inventory.markDirty();
			}
		}

		return new ShippingRemovalResult(pendingItems, totalValue);
	}

}
