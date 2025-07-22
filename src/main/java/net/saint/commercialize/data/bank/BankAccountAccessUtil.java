package net.saint.commercialize.data.bank;

import static net.saint.commercialize.util.Values.ifPresent;
import static net.saint.commercialize.util.Values.returnIfPresent;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.saint.commercialize.Commercialize;

public final class BankAccountAccessUtil {

	// Read

	public static int getBankAccountBalanceForPlayer(PlayerEntity player) {
		return returnIfPresent(getBankAccountForPlayer(player), account -> {
			return account.getBalance();
		}, 0);
	}

	// Write

	public static void deductAccountBalanceForPlayer(PlayerEntity player, int amount) {
		ifPresent(getBankAccountForPlayer(player), account -> {
			account.deduct(amount);
		});
	}

	public static void depositAccountBalanceForPlayer(PlayerEntity player, int amount) {
		ifPresent(getBankAccountForPlayer(player), account -> {
			account.deposit(amount);
		});
	}

	public static void deductAccountBalanceForCard(ItemStack itemStack, int amount) {
		ifPresent(getBankAccountForCard(itemStack), account -> {
			account.deduct(amount);
		});
	}

	public static void depositAccountBalanceForCard(ItemStack itemStack, int amount) {
		ifPresent(getBankAccountForCard(itemStack), account -> {
			account.deposit(amount);
		});
	}

	// Access

	public static BankAccount getBankAccountForCard(ItemStack itemStack) {
		var boundAccountId = CardItem.get(itemStack);

		if (boundAccountId == null) {
			Commercialize.LOGGER.warn("Could not resolve account id from provided payment card.");
			return null;
		}

		var account = Numismatics.BANK.getAccount(boundAccountId);

		if (account == null) {
			Commercialize.LOGGER.warn("Could not access bank account for card bound to account id '{}'.", boundAccountId);
			return null;
		}

		return account;
	}

	public static BankAccount getBankAccountForPlayer(PlayerEntity player) {
		if (player.getWorld().isClient()) {
			Commercialize.LOGGER.error("Can not access bank account on client-side.");
			return null;
		}

		var account = Numismatics.BANK.getAccount(player);

		if (account == null) {
			Commercialize.LOGGER.warn("Could not access bank account for player '{}'.", player.getId());
			return null;
		}

		return account;
	}

}
