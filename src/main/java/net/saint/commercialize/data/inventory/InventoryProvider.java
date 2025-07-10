package net.saint.commercialize.data.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;

@FunctionalInterface
public interface InventoryProvider {
	Inventory get(PlayerEntity player);
}
