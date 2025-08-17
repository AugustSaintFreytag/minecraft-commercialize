package net.saint.commercialize.screen.selling;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
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

public class SellingScreenHandler extends ScreenHandler implements SellingScreenDelegateHandler {

	// Library

	public record InitMessage(BlockPos position) {
	}

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "selling_screen_handler");

	// Properties

	public final PlayerInventory playerInventory;
	public final SimpleInventory blockInventory;

	public BlockPos position;

	public SellingScreen screen;

	// State

	private SellingScreenState state = new SellingScreenState();

	// Init

	public SellingScreenHandler(int syncId, PlayerInventory playerInventory) {
		// Convenience initializer to satisfy constraints. Actual construction is done in owning block entity.
		this(syncId, BlockPos.ORIGIN, playerInventory, new SimpleInventory(1));
	}

	public SellingScreenHandler(int syncId, BlockPos position, PlayerInventory playerInventory, SimpleInventory blockInventory) {
		super(ModScreenHandlers.SELLING_SCREEN_HANDLER, syncId);

		this.playerInventory = playerInventory;
		this.blockInventory = blockInventory;
		this.position = position;

		makeSlotsForBlockInventory(blockInventory);
		makeSlotsForPlayerInventory(playerInventory);

		initListeners();
		initNetworking();
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
		addServerboundMessage(C2SStateSyncMessage.class, message -> {
			var server = this.player().getServer();

			server.execute(() -> {
				var world = this.player().getWorld();
				var blockEntity = world.getBlockEntity(this.position);

				if (!(blockEntity instanceof ShippingBlockEntity)) {
					Commercialize.LOGGER
							.error("Expected block entity owning selling screen handler to be of type 'ShippingBlockEntity', got '"
									+ blockEntity.getClass().getName() + "'.");
					return;
				}

				var shippingBlockEntity = (ShippingBlockEntity) blockEntity;
				var state = message.toState();

				shippingBlockEntity.setSellingScreenState(state);
			});
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

	// Library

	private record C2SStateSyncMessage(ItemStack selectedItem, int offerPrice, long offerDuration, OfferPostStrategy offerPostStrategy) {

		public SellingScreenState toState() {
			var state = new SellingScreenState();

			state.selectedItem = this.selectedItem;
			state.offerPrice = this.offerPrice;
			state.offerDuration = this.offerDuration;
			state.offerPostStrategy = this.offerPostStrategy;

			return state;
		}

	}

}
