package net.saint.commercialize.data.inventory;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.saint.commercialize.data.payment.Currency;

public class PlayerInventoryCashUtil {

	// References

	private static final Map<String, InventoryProvider> INVENTORY_PROVIDERS = new HashMap<>();

	// Init

	public static void initialize() {
		INVENTORY_PROVIDERS.put("main", MainInventoryProvider.playerMainInventoryProvider());

		if (FabricLoader.getInstance().isModLoaded("backpacked")) {
			INVENTORY_PROVIDERS.put("backpacked", BackpackInventoryProvider.playerBackpackInventoryProvider());
		}
	}

	// Logic

	public static int getCurrencyValueInPlayerInventory(PlayerEntity player) {
		var totalValue = 0;

		if (player == null) {
			return totalValue;
		}

		for (var provider : INVENTORY_PROVIDERS.values()) {
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

}
