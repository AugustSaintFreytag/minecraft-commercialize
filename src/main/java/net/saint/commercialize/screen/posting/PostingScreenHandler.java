package net.saint.commercialize.screen.posting;

import static net.saint.commercialize.screen.posting.PostingScreenStateNetworking.overrideStateFromMessage;
import static net.saint.commercialize.screen.posting.PostingScreenStateNetworking.stateFromMessage;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.posting.PostingBlockEntity;
import net.saint.commercialize.block.posting.PostingScreenDelegateHandler;
import net.saint.commercialize.data.market.MarketOfferPostingUtil;
import net.saint.commercialize.data.text.ItemDescriptionUtil;
import net.saint.commercialize.gui.slot.CustomSlot;
import net.saint.commercialize.init.ModScreenHandlers;
import net.saint.commercialize.init.ModSounds;
import net.saint.commercialize.screen.posting.PostingScreenActionNetworking.C2SClearOfferActionMessage;
import net.saint.commercialize.screen.posting.PostingScreenActionNetworking.C2SPostOfferActionMessage;
import net.saint.commercialize.screen.posting.PostingScreenActionNetworking.S2CPostOfferActionMessage;
import net.saint.commercialize.screen.posting.PostingScreenStateNetworking.C2SStateRequestMessage;
import net.saint.commercialize.screen.posting.PostingScreenStateNetworking.C2SStateSyncMessage;
import net.saint.commercialize.screen.posting.PostingScreenStateNetworking.S2CStateSyncMessage;
import net.saint.commercialize.util.LocalizationUtil;

public class PostingScreenHandler extends ScreenHandler implements PostingScreenDelegateHandler {

	// Library

	public record InitMessage(BlockPos position) {
	}

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "posting_screen_handler");

	// References

	public final PlayerInventory playerInventory;
	public final SimpleInventory blockInventory;

	public PostingBlockEntity owner;
	public PostingScreen screen;

	// Properties

	private PostingScreenState state = new PostingScreenState();

	// Init

	public PostingScreenHandler(int syncId, PlayerInventory playerInventory) {
		// Convenience initializer used on client-side.
		this(syncId, null, playerInventory, new SimpleInventory(1));
	}

	public PostingScreenHandler(int syncId, PostingBlockEntity owner, PlayerInventory playerInventory, SimpleInventory blockInventory) {
		super(ModScreenHandlers.POSTING_SCREEN_HANDLER, syncId);

		this.playerInventory = playerInventory;
		this.blockInventory = blockInventory;
		this.owner = owner;

		makeSlotsForBlockInventory(blockInventory);
		makeSlotsForPlayerInventory(playerInventory);

		initListeners();
		initNetworking();

		// Force attach player to screen handler because owo is just too fucking late.
		// Server/Client communication is not possible until player is attached but even 
		// right before the initial screen render, it's still not ready.
		((OwoScreenHandlerExtension) this).owo$attachToPlayer(playerInventory.player);

		onBeforeOpened();
	}

	private void initListeners() {
		blockInventory.addListener(inventory -> {
			var isServer = this.owner != null;

			if (isServer) {
				return;
			}

			var itemStack = inventory.getStack(0);
			var postStrategy = this.state.postStrategy;

			this.state.stack = itemStack;
			this.state.price = PostingScreenUtil.basePriceForItemStack(itemStack, postStrategy);

			if (this.screen != null) {
				this.screen.updateDisplay();
			}

			this.pushState();
		});
	}

	private void initNetworking() {
		// Server-bound messages are already running on the server main thread, no diversion needed.

		addServerboundMessage(C2SStateSyncMessage.class, message -> {
			var state = stateFromMessage(message);
			this.owner.setScreenState(state);
		});

		addServerboundMessage(C2SStateRequestMessage.class, message -> {
			var state = this.owner.getScreenState();
			var response = new S2CStateSyncMessage(state.stack, state.price, state.duration, state.postStrategy);

			sendMessage(response);
		});

		addServerboundMessage(C2SClearOfferActionMessage.class, message -> {
			this.dropInventory(player(), this.blockInventory);
		});

		addServerboundMessage(C2SPostOfferActionMessage.class, message -> {
			var world = this.owner.getWorld();
			var server = world.getServer();
			var player = (ServerPlayerEntity) this.player();
			var position = this.owner.getPos();
			var draft = message.draft();

			var result = MarketOfferPostingUtil.postOfferToMarket(server, player, draft);

			switch (result) {
				case SUCCESS: {
					this.blockInventory.clear();
					world.playSound(null, position, ModSounds.SHIPPING_CLOSE_SOUND, SoundCategory.BLOCKS, 0.75f, 0.75f);
					world.playSound(null, position, ModSounds.MAILBOX_DELIVERY_SOUND, SoundCategory.BLOCKS, 0.75f, 0.75f);
					break;
				}
				case OUT_OF_QUOTA: {
					world.playSound(null, position, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 1f, 0.5f);
					break;
				}
				case INVALID: {
					world.playSound(null, position, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 1f, 0.5f);
					break;
				}
				default: {
					world.playSound(null, position, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 1f, 0.5f);
					break;
				}
			}

			var response = new S2CPostOfferActionMessage(result, draft.stack());
			sendMessage(response);
		});

		addClientboundMessage(S2CStateSyncMessage.class, message -> {
			overrideStateFromMessage(this.state, message);
			screen.updateDisplay();
		});

		addClientboundMessage(S2CPostOfferActionMessage.class, message -> {
			switch (message.result()) {
				case SUCCESS: {
					var itemDescription = ItemDescriptionUtil.descriptionForItemStack(message.stack());
					var displayText = LocalizationUtil.localizedText("gui", "posting.posting_confirm", itemDescription);
					this.player().sendMessage(displayText, true);
					break;
				}
				case OUT_OF_QUOTA: {
					var displayText = LocalizationUtil.localizedText("gui", "posting.posting_error_out_of_quota");
					this.player().sendMessage(displayText, true);
					break;
				}
				case INVALID: {
					var displayText = LocalizationUtil.localizedText("gui", "posting.posting_error_invalid");
					this.player().sendMessage(displayText, true);
					break;
				}
				default: {
					var displayText = LocalizationUtil.localizedText("gui", "posting.posting_error_failure");
					this.player().sendMessage(displayText, true);
					break;
				}
			}
		});
	}

	// Slots

	private void makeSlotsForBlockInventory(Inventory inventory) {
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
	public PostingScreenState getState() {
		return this.state;
	}

	public void setState(PostingScreenState state) {
		this.state = state;
	}

	public void requestState() {
		var message = new C2SStateRequestMessage();
		sendMessage(message);
	}

	public void pushState() {
		var message = new C2SStateSyncMessage(this.state.stack, this.state.price, this.state.duration, this.state.postStrategy);
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

	public void onBeforeOpened() {
		if (this.owner == null) {
			return;
		}

		var world = this.owner.getWorld();
		var position = this.owner.getPos();

		world.playSound(null, position, ModSounds.SHIPPING_OPEN_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
	}

	public void onOpened(PostingScreen screen, PlayerEntity player) {
		this.screen = screen;
		screen.delegate = this;

		requestState();
	}

	public void onClosed(PostingScreen screen, PlayerEntity player) {
		var world = player.getWorld();

		if (world.isClient()) {
			super.onClosed(player);
			return;
		}

		player.giveItemStack(this.blockInventory.getStack(0));
		world.playSound(null, owner.getPos(), ModSounds.SHIPPING_CLOSE_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);

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
		pushState();
	}

	@Override
	public void confirmOfferPost() {
		var itemStack = this.state.stack;
		var price = this.state.price;
		var duration = this.state.duration;
		var postStrategy = this.state.postStrategy;

		var draft = new MarketOfferPostingUtil.OfferDraft(itemStack, price, duration, postStrategy);
		this.sendMessage(new C2SPostOfferActionMessage(draft));
	}

	@Override
	public void clearOfferPost() {
		this.sendMessage(new C2SClearOfferActionMessage());
	}

	@Override
	public void resetOfferPrice() {
		var itemStack = this.state.stack;
		var postStrategy = this.state.postStrategy;

		this.state.price = PostingScreenUtil.basePriceForItemStack(itemStack, postStrategy);
		this.screen.updateDisplay();
	}

}
