package net.saint.commercialize.screen.shipping;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.shipping.ShippingBlockInventory;
import net.saint.commercialize.init.ModScreenHandlers;

public class ShippingBlockScreenHandler extends ScreenHandler {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_block_screen_handler");

	// Properties

	private final ShippingBlockInventory inventory;

	// Init

	public ShippingBlockScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new ShippingBlockInventory());
	}

	public ShippingBlockScreenHandler(int syncId, PlayerInventory playerInventory, ShippingBlockInventory inventory) {
		super(ModScreenHandlers.SHIPPING_BLOCK_SCREEN_HANDLER, syncId);
		this.inventory = inventory;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'quickMove'");
	}

}
