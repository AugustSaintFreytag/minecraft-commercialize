package net.saint.commercialize.screen.selling;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.gui.slot.CustomSlot;
import net.saint.commercialize.init.ModScreenHandlers;

public class SellingScreenHandler extends ScreenHandler {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "selling_screen_handler");

	// Properties

	public final PlayerInventory playerInventory;
	public final SimpleInventory blockInventory;
	public final BlockPos position;

	// Init

	public SellingScreenHandler(int syncId, PlayerInventory playerInventory) {
		// Convenience initializer to satisfy constraints. Actual construction is done in owning block entity.
		this(syncId, playerInventory, new SimpleInventory(1), BlockPos.ORIGIN);
	}

	public SellingScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory blockInventory, BlockPos position) {
		super(ModScreenHandlers.SELLING_SCREEN_HANDLER, syncId);

		this.playerInventory = playerInventory;
		this.blockInventory = blockInventory;
		this.position = position;

		makeSlotsForBlockInventory(blockInventory);
		makeSlotsForPlayerInventory(playerInventory);
	}

	// Slots

	private void makeSlotsForBlockInventory(SimpleInventory inventory) {
		SlotGenerator.begin(this::addSlot, 68, -15).slotFactory((_inventory, index, x, y) -> {
			return new CustomSlot(_inventory, index, x, y);
		}).grid(inventory, 0, 1, 1);
	}

	private void makeSlotsForPlayerInventory(PlayerInventory inventory) {
		SlotGenerator.begin(this::addSlot, -4, 108).slotFactory((_inventory, index, x, y) -> {
			return new CustomSlot(_inventory, index, x, y);
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

	@Override
	public void onClosed(PlayerEntity player) {
		var world = player.getWorld();

		if (world.isClient()) {
			super.onClosed(player);
			return;
		}

		ItemScatterer.spawn(world, player.getBlockPos(), this.blockInventory);
		super.onClosed(player);
	}

}
