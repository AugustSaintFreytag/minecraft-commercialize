package net.saint.commercialize.data.shipping;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.ithundxr.createnumismatics.content.bank.CardItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.shipping.ShippingBlockEntity;
import net.saint.commercialize.block.shipping.ShippingBlockInventory;
import net.saint.commercialize.data.bank.BankAccountAccessUtil;
import net.saint.commercialize.data.inventory.InventoryCashUtil;
import net.saint.commercialize.data.item.ItemListUtil;
import net.saint.commercialize.data.item.ItemValueUtil;

public final class ShippingExchangeTickingUtil {

	// Library

	public record ShippingAssortment(List<ItemStack> items, List<Integer> slots, int value) {
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
		var assortment = prepareItemsForShippingFromInventory(world, inventory);

		if (assortment.value == 0) {
			callback.accept(ShippingTickResult.NO_ITEMS);
			return;
		}

		var saleValue = (int) (assortment.value * Commercialize.CONFIG.sellingPriceFactor * randomSellingPriceJitterFactor(world));
		var paymentCard = blockEntity.inventory.getCardStack();
		var playerHasPaymentCard = !paymentCard.isEmpty();

		// Deposit to Account

		if (playerHasPaymentCard) {
			var boundAccountId = CardItem.get(paymentCard);
			var boundBankAccount = BankAccountAccessUtil.getBankAccountForCard(paymentCard);

			if (boundBankAccount == null) {
				Commercialize.LOGGER.warn(
						"Could not get bank account from provided payment card with id '{}' in shipping block.",
						boundAccountId
				);
				callback.accept(ShippingTickResult.FAILURE);
				return;
			}

			var bankAccountOwnerName = BankAccountAccessUtil.getOwnerNameForBankAccount(world.getServer(), boundBankAccount);
			Commercialize.LOGGER.info(
					"Depositing {} ¤ to bank account '{}' ({}) from provided payment card in shipping block.",
					saleValue,
					bankAccountOwnerName,
					boundAccountId
			);

			var preDepositBalance = boundBankAccount.getBalance();
			BankAccountAccessUtil.depositAccountBalanceForCard(paymentCard, saleValue);
			var postDepositBalance = boundBankAccount.getBalance();

			if (preDepositBalance + saleValue != postDepositBalance) {
				Commercialize.LOGGER.error(
						"Bank account '{}' ({}) balance did not update correctly after depositing {} ¤ from shipping block payment card. "
								+ "Pre-deposit balance: {} ¤, post-deposit balance: {} ¤.",
						bankAccountOwnerName,
						boundAccountId,
						saleValue,
						preDepositBalance,
						postDepositBalance
				);
				callback.accept(ShippingTickResult.FAILURE);
				return;
			}

			removeItemsForShippingFromInventory(inventory, assortment);

			Commercialize.LOGGER.info(
					"Sold {} item(s) from shipping block, deposited {} ¤ to bank account '{}' ({}) from shipping block payment card.",
					assortment.items.size(),
					saleValue,
					bankAccountOwnerName,
					boundAccountId
			);

			callback.accept(ShippingTickResult.SOLD);
			return;
		}

		// Deposit in Cash

		removeItemsForShippingFromInventory(inventory, assortment);

		var remainingCurrencyItems = InventoryCashUtil.addCurrencyToInventory(inventory.main, saleValue);
		dropItemStacksInWorld(world, blockEntity.getPos(), remainingCurrencyItems);

		if (remainingCurrencyItems.size() > 0) {
			Commercialize.LOGGER.info(
					"Sold {} item(s) from shipping block, paid out {} ¤ in cash ({} ¤ dropped in world due to full inventory).",
					assortment.items.size(),
					saleValue,
					InventoryCashUtil.getCurrencyValueInList(remainingCurrencyItems)
			);
		} else {
			Commercialize.LOGGER.info("Sold {} item(s) from shipping block, paid out {} ¤ in cash.", assortment.items.size(), saleValue);
		}

		callback.accept(ShippingTickResult.SOLD);
	}

	private static void dropItemStacksInWorld(World world, BlockPos position, List<ItemStack> itemStacks) {
		var itemList = ItemListUtil.defauledItemStackListFromList(itemStacks);
		ItemScatterer.spawn(world, position, itemList);
	}

	private static double randomSellingPriceJitterFactor(World world) {
		return 1 + world.getRandom().nextTriangular(0, 0.05);
	}

	// Shipping Assortment

	public static ShippingAssortment prepareItemsForShippingFromInventory(World world, ShippingBlockInventory inventory) {
		var slots = new ArrayList<Integer>();
		var itemStacks = new ArrayList<ItemStack>();
		var totalValue = 0;

		for (var slot : ShippingBlockInventory.MAIN_SLOTS) {
			var itemStack = inventory.getStack(slot);
			var stackValue = ItemValueUtil.getValueForItemStack(itemStack);

			// Item has no value and can not be sold.
			if (stackValue == 0) {
				continue;
			}

			slots.add(slot);
			itemStacks.add(itemStack);
			totalValue += stackValue;
		}

		return new ShippingAssortment(itemStacks, slots, totalValue);
	}

	public static void removeItemsForShippingFromInventory(ShippingBlockInventory inventory, ShippingAssortment assortment) {
		for (var slot : assortment.slots) {
			inventory.removeStack(slot);
			inventory.markDirty();
		}
	}

}
