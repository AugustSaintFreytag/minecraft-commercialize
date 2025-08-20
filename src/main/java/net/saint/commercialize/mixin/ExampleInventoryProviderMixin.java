package net.saint.commercialize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.saint.commercialize.data.inventory.InventoryAccessUtil;

@Mixin(InventoryAccessUtil.class)
public abstract class ExampleInventoryProviderMixin {

	@Inject(method = "initialize", at = @At("TAIL"), remap = false)
	private static void mixinInitialize(CallbackInfo callbackInfo) {
		// Load mod services providing inventory access.
		InventoryAccessUtil.registerInventoryProvider(player -> {
			// This is a placeholder for the default inventory provider.
			// Use the mod's available types to get a custom inventory reference.
			return player.getInventory();
		});
	}

}