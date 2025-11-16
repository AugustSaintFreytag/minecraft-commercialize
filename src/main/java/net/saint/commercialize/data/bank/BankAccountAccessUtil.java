package net.saint.commercialize.data.bank;

import static net.saint.commercialize.util.Values.ifPresent;
import static net.saint.commercialize.util.Values.returnIfPresent;

import java.util.UUID;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.BankAccount.Type;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.player.PlayerProfileAccessUtil;

public final class BankAccountAccessUtil {

	// Read

	public static int getBankAccountBalanceForPlayer(UUID playerId) {
		return returnIfPresent(getBankAccountForPlayerById(playerId), account -> {
			return account.getBalance();
		}, 0);
	}

	public static int getBankAccountBalanceForCard(ItemStack itemStack) {
		return returnIfPresent(getBankAccountForCard(itemStack), account -> {
			return account.getBalance();
		}, 0);
	}

	// Write

	public static void deductAccountBalanceForPlayer(UUID playerId, int amount) {
		ifPresent(getBankAccountForPlayerById(playerId), account -> {
			account.deduct(amount);
		});
	}

	public static void depositAccountBalanceForPlayer(UUID playerId, int amount) {
		ifPresent(getBankAccountForPlayerById(playerId), account -> {
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

	public static String getOwnerNameForCard(ItemStack itemStack) {
		return CardItem.getPlayerName(itemStack);
	}

	public static String getOwnerNameForBankAccount(MinecraftServer server, BankAccount account) {
		return PlayerProfileAccessUtil.getPlayerNameById(server, account.id);
	}

	public static BankAccount getBankAccountForCard(ItemStack itemStack) {
		var boundAccountId = CardItem.get(itemStack);

		if (boundAccountId == null) {
			Commercialize.LOGGER.warn("Could not resolve account id from provided payment card.");
			return null;
		}

		var account = Numismatics.BANK.getOrCreateAccount(boundAccountId, Type.PLAYER);

		if (account == null) {
			Commercialize.LOGGER.warn("Could not access bank account for card bound to account id '{}'.", boundAccountId);
			return null;
		}

		return account;
	}

	public static BankAccount getBankAccountForPlayerById(UUID playerId) {
		var account = Numismatics.BANK.getOrCreateAccount(playerId, Type.PLAYER);

		if (account == null) {
			Commercialize.LOGGER.warn("Could not access bank account for player '{}'.", playerId);
			return null;
		}

		return account;
	}

	public static boolean isPaymentCard(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return false;
		}
		return NumismaticsItems.CARDS.contains(itemStack.getItem());
	}

}
