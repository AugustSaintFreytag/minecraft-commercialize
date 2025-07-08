package net.saint.commercialize.data.inventory;

import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.mrcrayfish.backpacked.platform.Services;

public class BackpackInventoryProvider {

	public static InventoryProvider playerBackpackInventoryProvider() {
		return player -> {
			var backpackStack = Services.BACKPACK.getBackpackStack(player);

			if (backpackStack == null || backpackStack.isEmpty()) {
				return null;
			}

			var backpackInventory = ((BackpackedInventoryAccess) player).backpacked$GetBackpackInventory();
			return backpackInventory;

		};
	}

}
