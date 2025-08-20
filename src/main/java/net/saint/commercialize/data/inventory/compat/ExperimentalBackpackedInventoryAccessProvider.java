package net.saint.commercialize.data.inventory.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.inventory.InventoryAccessUtil;

public final class ExperimentalBackpackedInventoryAccessProvider {

	private static final String BACKPACKED_SERVICES_CLASS = "com.mrcrayfish.backpacked.platform.Services";
	private static final String BACKPACKED_INVENTORY_ACCESS_METHOD = "backpacked$GetBackpackInventory";

	public static void register() {
		try {
			// Use a class loader check to see if the mod is present.
			Class.forName(BACKPACKED_SERVICES_CLASS);

			// If the class is found, proceed with reflection.
			registerBackpackedProvider();

		} catch (ClassNotFoundException e) {
			Commercialize.LOGGER.info("Could not detect Backpacked mod, skipping inventory provider registration.");
			// Backpacked mod is not present, do nothing.
		} catch (Exception e) {
			// Log other reflection-related errors if necessary
		}
	}

	private static void registerBackpackedProvider() throws Exception {
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		// Get Services.BACKPACK field
		Class<?> servicesClass = Class.forName(BACKPACKED_SERVICES_CLASS);
		Field backpackServiceField = servicesClass.getField("BACKPACK");
		Object backpackService = backpackServiceField.get(null);

		// Get IBackpackService#getBackpackStack(Player) method
		Method getBackpackStackMethod = backpackService.getClass().getMethod("getBackpackStack", PlayerEntity.class);
		MethodHandle getBackpackStackHandle = lookup.unreflect(getBackpackStackMethod);

		// Get BackpackedInventoryAccess#backpacked$GetBackpackInventory() method
		Method getBackpackInventoryMethod = PlayerEntity.class.getMethod(BACKPACKED_INVENTORY_ACCESS_METHOD);
		MethodHandle getBackpackInventoryHandle = lookup.unreflect(getBackpackInventoryMethod);

		InventoryAccessUtil.registerInventoryProvider(player -> {
			try {
				var backpackStack = (ItemStack) getBackpackStackHandle.invoke(backpackService, player);

				if (backpackStack == null || backpackStack.isEmpty()) {
					return null;
				}

				return (Inventory) getBackpackInventoryHandle.invoke(player);
			} catch (Throwable e) {
				// This will catch invocation errors if the method handles fail.
				Commercialize.LOGGER.error("Failed to access Backpacked inventory for player: " + player.getName().getString() + ".", e);
				return null;
			}
		});
	}

}
