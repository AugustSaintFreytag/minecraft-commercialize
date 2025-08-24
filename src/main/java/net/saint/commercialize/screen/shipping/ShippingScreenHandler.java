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
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.shipping.ShippingBlockEntity;
import net.saint.commercialize.block.shipping.ShippingBlockInventory;
import net.saint.commercialize.init.ModScreenHandlers;
import net.saint.commercialize.init.ModSounds;

public class ShippingScreenHandler extends ScreenHandler {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_screen_handler");

	// Properties

	public final ShippingBlockEntity owner;
	public final PlayerInventory playerInventory;
	public final ShippingBlockInventory blockInventory;

	public ShippingScreen screen;

	// Init

	public ShippingScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, null, playerInventory, new ShippingBlockInventory());
	}

	public ShippingScreenHandler(int syncId, ShippingBlockEntity owner, PlayerInventory playerInventory,
			ShippingBlockInventory blockInventory) {
		super(ModScreenHandlers.SHIPPING_SCREEN_HANDLER, syncId);

		this.owner = owner;
		this.playerInventory = playerInventory;
		this.blockInventory = blockInventory;

		makeSlotsForBlockInventory(blockInventory);
		makeSlotForPaymentCard(blockInventory);
		makeSlotsForPlayerInventory(playerInventory);

		onBeforeOpened();
	}

	// Lifecycle

	public void onBeforeOpened() {
		if (this.owner == null) {
			return;
		}

		var world = this.owner.getWorld();
		var position = this.owner.getPos();

		world.playSound(null, position, ModSounds.SHIPPING_OPEN_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
	}

	public void onOpened(ShippingScreen screen, PlayerEntity player) {
		this.screen = screen;
	}

	@Override
	public void onClosed(PlayerEntity player) {
		onClosed(this.screen, player);
	}

	public void onClosed(ShippingScreen screen, PlayerEntity player) {
		this.screen = null;
		var world = player.getWorld();

		if (world.isClient()) {
			return;
		}

		world.playSound(null, this.owner.getPos(), ModSounds.SHIPPING_CLOSE_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
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
