package net.saint.commercialize.data.inventory;

import java.util.ArrayList;
import java.util.List;

public final class InventoryAccessUtil {

	// References

	private static final List<InventoryProvider> INVENTORY_PROVIDERS = new ArrayList<>();

	// Init

	/**
	 * Registers all default inventory providers.
	 * May be injected to via mixin to register custom mod inventory providers.
	 */
	public static void initialize() {
		registerInventoryProvider(player -> player.getInventory());
	}

	// Access

	public static List<InventoryProvider> getInventoryProviders() {
		return INVENTORY_PROVIDERS;
	}

	// Mutation

	public static void registerInventoryProvider(InventoryProvider provider) {
		INVENTORY_PROVIDERS.add(provider);
	}

}
