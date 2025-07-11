package net.saint.commercialize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.mrcrayfish.backpacked.platform.Services;

import net.saint.commercialize.data.inventory.InventoryAccessUtil;

@Mixin(InventoryAccessUtil.class)
public abstract class BackpackedInventoryProviderMixin {

	// This mixin is used to register the Backpacked inventory provider
	// It allows the Backpacked mod to provide its own inventory implementation
	// without modifying the original InventoryAccessUtil class directly.
	// The mixin is excluded from loading if the Backpacked mod is not present.

	@Inject(method = "initialize", at = @At("TAIL"), remap = false)
	private static void mixinInitialize() {
		InventoryAccessUtil.registerInventoryProvider(player -> {
			var backpackStack = Services.BACKPACK.getBackpackStack(player);

			if (backpackStack == null || backpackStack.isEmpty()) {
				return null;
			}

			var backpackInventory = ((BackpackedInventoryAccess) player).backpacked$GetBackpackInventory();
			return backpackInventory;

		});
	}

}
