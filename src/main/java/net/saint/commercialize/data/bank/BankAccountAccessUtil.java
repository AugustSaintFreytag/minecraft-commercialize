package net.saint.commercialize.data.bank;

import static net.saint.commercialize.util.Values.ifPresent;
import static net.saint.commercialize.util.Values.returnIfPresent;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import net.minecraft.entity.player.PlayerEntity;
import net.saint.commercialize.Commercialize;

public final class BankAccountAccessUtil {

	public static int getBankAccountBalanceForPlayer(PlayerEntity player) {
		return returnIfPresent(getBankAccountForPlayer(player), account -> {
			return account.getBalance();
		}, 0);
	}

	public static void deductAccountBalanceForPlayer(PlayerEntity player, int amount) {
		ifPresent(getBankAccountForPlayer(player), account -> {
			account.deduct(amount);
		});
	}

	public static BankAccount getBankAccountForPlayer(PlayerEntity player) {
		if (player.getWorld().isClient()) {
			Commercialize.LOGGER.error("Can not query player bank account balance on client-side.");
			return null;
		}

		return Numismatics.BANK.getAccount(player);
	}

}
