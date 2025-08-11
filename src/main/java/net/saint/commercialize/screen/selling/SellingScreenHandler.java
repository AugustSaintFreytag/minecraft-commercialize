package net.saint.commercialize.screen.selling;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.init.ModScreenHandlers;

public class SellingScreenHandler extends ScreenHandler {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "selling_screen_handler");

	// Properties

	public final PlayerInventory playerInventory;
	public final SimpleInventory blockInventory;

	// Init

	public SellingScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(1));
	}

	public SellingScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory blockInventory) {
		super(ModScreenHandlers.SELLING_SCREEN_HANDLER, syncId);

		this.playerInventory = playerInventory;
		this.blockInventory = blockInventory;

		makeSlotsForBlockInventory(blockInventory);
		makeSlotsForPlayerInventory(playerInventory);
	}

	// Slots

	private void makeSlotsForBlockInventory(SimpleInventory inventory) {
		SlotGenerator.begin(this::addSlot, 68, -15).slotFactory((_inventory, index, x, y) -> {
			return new Slot(_inventory, index, x, y);
		}).grid(inventory, 0, 1, 1);
	}

	private void makeSlotsForPlayerInventory(PlayerInventory inventory) {
		SlotGenerator.begin(this::addSlot, -4, 108).slotFactory((_inventory, index, x, y) -> {
			return new Slot(_inventory, index, x, y);
		}).playerInventory(inventory);
	}

	// Interaction

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.blockInventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ScreenUtils.handleSlotTransfer(this, slot, this.blockInventory.size());
	}

}
