package net.saint.commercialize.screen.selling;

import static net.saint.commercialize.screen.selling.SellingScreenStateSync.overrideStateFromMessage;
import static net.saint.commercialize.screen.selling.SellingScreenStateSync.stateFromMessage;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.shipping.SellingScreenDelegateHandler;
import net.saint.commercialize.block.shipping.ShippingBlockEntity;
import net.saint.commercialize.gui.slot.CustomSlot;
import net.saint.commercialize.init.ModScreenHandlers;
import net.saint.commercialize.screen.selling.SellingScreenStateSync.C2SStateRequestMessage;
import net.saint.commercialize.screen.selling.SellingScreenStateSync.C2SStateSyncMessage;
import net.saint.commercialize.screen.selling.SellingScreenStateSync.S2CStateSyncMessage;

public class SellingScreenHandler extends ScreenHandler implements SellingScreenDelegateHandler {

	// Library

	public record InitMessage(BlockPos position) {
	}

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "selling_screen_handler");

	// Properties

	public final PlayerInventory playerInventory;
	public final SimpleInventory blockInventory;

	public ShippingBlockEntity owner;

	public SellingScreen screen;

	// State

	private SellingScreenState state = new SellingScreenState();

	// Init

	public SellingScreenHandler(int syncId, PlayerInventory playerInventory) {
		// Convenience initializer to satisfy constraints. Actual construction is done in owning block entity.
		this(syncId, null, playerInventory, new SimpleInventory(1));
	}

	public SellingScreenHandler(int syncId, ShippingBlockEntity owner, PlayerInventory playerInventory, SimpleInventory blockInventory) {
		super(ModScreenHandlers.SELLING_SCREEN_HANDLER, syncId);

		this.playerInventory = playerInventory;
		this.blockInventory = blockInventory;
		this.owner = owner;

		makeSlotsForBlockInventory(blockInventory);
		makeSlotsForPlayerInventory(playerInventory);

		initListeners();
		initNetworking();

		// Force attach player to screen handler because owo is just too fucking late.
		((OwoScreenHandlerExtension) this).owo$attachToPlayer(playerInventory.player);
	}

	private void initListeners() {
		blockInventory.addListener(inventory -> {
			var itemStack = inventory.getStack(0);
			this.state.selectedItem = itemStack;

			if (!itemStack.isEmpty()) {
				var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
				var itemBaseValue = Commercialize.ITEM_MANAGER.getValueForItem(itemIdentifier);
				this.state.offerPrice = itemBaseValue * itemStack.getCount();
			} else {
				this.state.offerPrice = 0;
			}

			if (this.screen != null) {
				this.screen.updateDisplay();
			}
		});
	}

	private void initNetworking() {
		// Server-bound messages are already running on the server main thread, no diversion needed.

		addServerboundMessage(C2SStateSyncMessage.class, message -> {
			var state = stateFromMessage(message);
			this.owner.setSellingScreenState(state);
		});

		addServerboundMessage(C2SStateRequestMessage.class, message -> {
			var state = this.owner.getSellingScreenState();
			var response = new S2CStateSyncMessage(state.selectedItem, state.offerPrice, state.offerDuration, state.offerPostStrategy);
			sendMessage(response);
		});

		addClientboundMessage(S2CStateSyncMessage.class, message -> {
			overrideStateFromMessage(this.state, message);
			screen.updateDisplay();
		});
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

	// Access

	@Override
	public SellingScreenState getState() {
		return this.state;
	}

	public void setState(SellingScreenState state) {
		this.state = state;
	}

	public void requestState() {
		var message = new C2SStateRequestMessage();
		sendMessage(message);
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

	// Lifecycle

	public void onOpened(SellingScreen screen, PlayerEntity player) {
		this.screen = screen;
		screen.delegate = this;

		requestState();
	}

	public void onClosed(SellingScreen screen, PlayerEntity player) {
		var world = player.getWorld();

		if (world.isClient()) {
			super.onClosed(player);
			return;
		}

		ItemScatterer.spawn(world, player.getBlockPos(), this.blockInventory);
		super.onClosed(player);
	}

	@Override
	public void onClosed(PlayerEntity player) {
		onClosed(this.screen, player);
	}

	// Delegation

	@Override
	public void onScreenClose() {
	}

	@Override
	public void onScreenUpdate() {
		// Called from client-side after screen update.
		var message = new C2SStateSyncMessage(this.state.selectedItem, this.state.offerPrice, this.state.offerDuration,
				this.state.offerPostStrategy);
		sendMessage(message);
	}

	@Override
	public void confirmOfferPost() {
	}

	@Override
	public void clearOfferPost() {
	}

	@Override
	public void resetOfferPrice() {
	}

}
