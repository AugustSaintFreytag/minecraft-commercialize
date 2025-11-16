package net.saint.commercialize.data.market;

import java.util.function.Predicate;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.commercialize.data.bank.BankAccountAccessUtil;
import net.saint.commercialize.data.inventory.InventoryCashUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.payment.PaymentMethod;

public final class MarketOfferFilteringUtil {

	// Blocks (Filtering)

	public static Predicate<Offer> offerFilterPredicate(ServerPlayerEntity player, OfferFilterMode filterMode,
			PaymentMethod paymentMethod) {
		if (filterMode == null) {
			filterMode = OfferFilterMode.ALL;
		}

		switch (filterMode) {
			case AFFORDABLE:
				var balance = playerBalanceForPaymentMethod(player, paymentMethod);
				return offer -> offer.price <= balance;
			default:
				return offer -> true;
		}
	}

	public static Predicate<Offer> searchFilterPredicate(ServerPlayerEntity player, String searchTerm) {
		if (searchTerm.isEmpty()) {
			return offer -> true;
		}

		var sanitizedSearchTerm = searchTerm.toLowerCase();
		return offer -> offerMatchesSearchTerm(offer, player, sanitizedSearchTerm);
	}

	// Search

	private static boolean offerMatchesSearchTerm(Offer offer, ServerPlayerEntity player, String searchTerm) {
		var stack = offer.stack;
		var item = stack.getItem();
		var itemId = Registries.ITEM.getId(item);

		var itemName = stack.getName().getString().toLowerCase();
		var itemNamespace = itemId.getNamespace().toLowerCase();
		var itemTooltipLines = offer.stack.getTooltip(player, TooltipContext.BASIC).stream().map(line -> line.getString()).toList();
		var itemTooltip = String.join("", itemTooltipLines).toLowerCase();
		var sellerName = offer.sellerName.toLowerCase();

		var didMatch = itemName.contains(searchTerm) || itemNamespace.contains(searchTerm) || itemTooltip.contains(searchTerm)
				|| sellerName.contains(searchTerm);

		return didMatch;
	}

	// Balance

	private static int playerBalanceForPaymentMethod(PlayerEntity player, PaymentMethod paymentMethod) {
		switch (paymentMethod) {
			case INVENTORY:
				return InventoryCashUtil.getCurrencyValueInAnyInventoriesForPlayer(player);
			case ACCOUNT:
				return BankAccountAccessUtil.getBankAccountBalanceForPlayer(player.getUuid());
			default:
				return 0;
		}
	}

}
