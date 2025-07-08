package net.saint.commercialize.data.inventory;

public class MainInventoryProvider {

	public static InventoryProvider playerMainInventoryProvider() {
		return player -> player.getInventory();
	}

}
