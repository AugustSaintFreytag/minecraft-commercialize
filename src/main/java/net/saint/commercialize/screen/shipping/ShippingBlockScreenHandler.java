package net.saint.commercialize.screen.shipping;

import dev.ithundxr.createnumismatics.content.bank.CardSlot;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.shipping.ShippingBlockInventory;
import net.saint.commercialize.init.ModScreenHandlers;
import net.saint.commercialize.init.ModSounds;

public class ShippingBlockScreenHandler extends ScreenHandler {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_block_screen_handler");

	// Properties

	public final BlockPos position;
	public final PlayerInventory playerInventory;
	public final ShippingBlockInventory blockInventory;

	// Init

	public ShippingBlockScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, BlockPos.ORIGIN, playerInventory, new ShippingBlockInventory());
	}

	public ShippingBlockScreenHandler(int syncId, BlockPos position, PlayerInventory playerInventory,
			ShippingBlockInventory blockInventory) {
		super(ModScreenHandlers.SHIPPING_BLOCK_SCREEN_HANDLER, syncId);

		this.position = position;
		this.playerInventory = playerInventory;
		this.blockInventory = blockInventory;

		makeSlotsForBlockInventory(blockInventory);
		makeSlotForPaymentCard(blockInventory);
		makeSlotsForPlayerInventory(playerInventory);
	}

	public void onOpen(PlayerEntity player) {
		var world = player.getWorld();

		if (world.isClient()) {
			return;
		}

		world.playSound(null, position, ModSounds.SHIPPING_OPEN_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		var world = player.getWorld();

		if (world.isClient()) {
			return;
		}

		world.playSound(null, position, ModSounds.SHIPPING_CLOSE_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
		super.onClosed(player);
	}

	// Slots

	private void makeSlotsForBlockInventory(ShippingBlockInventory inventory) {
		SlotGenerator.begin(this::addSlot, -7, 4).slotFactory((_inventory, index, x, y) -> {
			return new Slot(_inventory, index, x, y);
		}).grid(inventory.main, 0, 9, 1);
	}

	private void makeSlotForPaymentCard(ShippingBlockInventory inventory) {
		SlotGenerator.begin(this::addSlot, 167, 4).slotFactory((_inventory, index, x, y) -> {
			return new CardSlot(_inventory, index, x, y);
		}).grid(inventory.card, 0, 1, 1);
	}

	private void makeSlotsForPlayerInventory(PlayerInventory inventory) {
		SlotGenerator.begin(this::addSlot, 10, 89).slotFactory((_inventory, index, x, y) -> {
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
