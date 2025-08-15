package net.saint.commercialize.block.market;

import static net.saint.commercialize.util.Values.returnIfPresentAsString;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;
import net.saint.commercialize.init.ModBlockEntities;
import net.saint.commercialize.init.ModSounds;
import net.saint.commercialize.network.MarketC2SOrderMessage;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketC2SStateSyncMessage;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.network.MarketS2COrderMessage;
import net.saint.commercialize.screen.market.MarketScreen;
import net.saint.commercialize.screen.market.MarketScreenUtil;
import net.saint.commercialize.util.LocalizationUtil;

public class MarketBlockEntity extends BlockEntity implements MarketBlockScreenHandler {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_block_entity");

	// Properties

	private MarketBlockEntityState state = new MarketBlockEntityState();

	protected long lastListingTick = 0;

	protected int lastListingHash = 0;

	private MarketScreen marketScreen;

	// Init

	public MarketBlockEntity(BlockPos position, BlockState state) {
		super(ModBlockEntities.MARKET_BLOCK_ENTITY, position, state);
	}

	// Access

	public MarketBlockEntityState getState() {
		return state;
	}

	public void setState(MarketBlockEntityState state) {
		this.state = state;
		markDirty();
	}

	// NBT

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);

		state.searchTerm = nbt.getString("searchTerm");
		state.sortMode = returnIfPresentAsString(nbt, "sortMode", value -> OfferSortMode.valueOf(value), OfferSortMode.ITEM_NAME);
		state.sortOrder = returnIfPresentAsString(nbt, "sortOrder", OfferSortOrder::valueOf, OfferSortOrder.ASCENDING);
		state.filterMode = returnIfPresentAsString(nbt, "filterMode", OfferFilterMode::valueOf, OfferFilterMode.ALL);
		state.paymentMethod = returnIfPresentAsString(nbt, "paymentMethod", PaymentMethod::valueOf, PaymentMethod.INVENTORY);
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		nbt.putString("searchTerm", state.searchTerm);
		nbt.putString("sortMode", state.sortMode.name());
		nbt.putString("sortOrder", state.sortOrder.name());
		nbt.putString("filterMode", state.filterMode.name());
		nbt.putString("paymentMethod", state.paymentMethod.name());

		super.writeNbt(nbt);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		var nbt = new NbtCompound();
		writeNbt(nbt);

		return nbt;
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	// Networking

	public void receiveListMessage(MarketS2CListMessage message) {
		state.balance = message.balance;
		state.cardOwner = message.cardOwner;

		state.marketOffers.clearOffers();
		state.marketOffers.addOffers(message.offers);
		state.marketOffers.setOffersAreCapped(message.isCapped);

		Commercialize.LOGGER.info("Received market data for market block entity at pos '{}' from server: {} offer(s) available.",
				this.getPos().toShortString(), state.marketOffers.getOffers().count());

		lastListingTick = world.getTimeOfDay();
		updateMarketScreen();
	}

	public void receiveOrderMessage(MarketS2COrderMessage message) {
		Commercialize.LOGGER.info("Received market order response with result '{}' for offer '{}' at pos '{}'.", message.result,
				message.offers, this.getPos().toShortString());

		var client = MinecraftClient.getInstance();
		var player = client.player;

		switch (message.result) {
			case INSUFFICIENT_FUNDS: {
				var displayText = LocalizationUtil.localizedText("gui", "market.order_error_insufficient_funds");
				player.sendMessage(displayText, true);
				player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
				break;
			}
			case INVIABLE_DELIVERY: {
				var displayText = LocalizationUtil.localizedText("gui", "market.order_error_inviable_delivery");
				player.sendMessage(displayText, true);
				player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
				break;
			}
			case INVIABLE_OFFERS: {
				var displayText = LocalizationUtil.localizedText("gui", "market.order_error_inviable_offers");
				player.sendMessage(displayText, true);
				player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
				break;
			}
			case INVIABLE_PAYMENT_METHOD: {
				var displayText = LocalizationUtil.localizedText("gui", "market.order_error_inviable_payment_method");
				player.sendMessage(displayText, true);
				player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
				break;
			}
			case FAILURE: {
				var displayText = LocalizationUtil.localizedText("gui", "market.order_error_failure");
				player.sendMessage(displayText, true);
				player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
				break;
			}
			case SUCCESS: {
				var offers = state.cartOffers.getOffers().toList();
				var itemNames = MarketScreenUtil.textForOrderSummary(offers);
				var formattedTotal = CurrencyFormattingUtil.formatCurrency(getCartTotal());
				var displayText = LocalizationUtil.localizedText("gui", "market.order_confirm_instant", itemNames, formattedTotal);

				player.sendMessage(displayText, true);
				player.playSound(ModSounds.ORDER_CONFIRM_SOUND, 1.0F, 1.0F);

				state.cartOffers.clearOffers();
				requestMarketData();
				updateMarketScreen();
				break;
			}
		}
	}

	public void sendStateSync(MarketBlockStateSyncReason reason) {
		var message = new MarketC2SStateSyncMessage();
		message.position = this.getPos();
		message.reason = reason;
		message.state = this.state;

		var buffer = PacketByteBufs.create();
		message.encodeToBuffer(buffer);

		ClientPlayNetworking.send(MarketC2SStateSyncMessage.ID, buffer);
	}

	public void requestMarketData() {
		var message = new MarketC2SQueryMessage();

		message.position = this.getPos();
		message.searchTerm = state.searchTerm;
		message.sortMode = state.sortMode;
		message.sortOrder = state.sortOrder;
		message.filterMode = state.filterMode;
		message.paymentMethod = state.paymentMethod;

		var buffer = PacketByteBufs.create();
		message.encodeToBuffer(buffer);

		ClientPlayNetworking.send(MarketC2SQueryMessage.ID, buffer);
	}

	public void confirmCartOrder() {
		var message = new MarketC2SOrderMessage();

		message.position = this.getPos();
		message.offers = state.cartOffers.getOffers().map(offer -> offer.id).toList();
		message.paymentMethod = state.paymentMethod;

		var buffer = PacketByteBufs.create();
		message.encodeToBuffer(buffer);

		ClientPlayNetworking.send(MarketC2SOrderMessage.ID, buffer);
	}

	// Ticking

	public static void tick(World world, BlockPos position, BlockState state, MarketBlockEntity blockEntity) {
		if (!world.isClient()) {
			return;
		}

		var currentTime = world.getTimeOfDay();
		var timeSinceLastListing = currentTime - blockEntity.lastListingTick;

		var isScreenActive = blockEntity.marketScreen != null;
		var shouldRefreshForActiveScreen = isScreenActive && timeSinceLastListing > Commercialize.CONFIG.listingRefreshInterval;
		var shouldRefreshForInactiveScreen = !isScreenActive && Commercialize.CONFIG.listingRefreshIntervalWhenInactive != -1
				&& timeSinceLastListing > Commercialize.CONFIG.listingRefreshIntervalWhenInactive;

		if (shouldRefreshForActiveScreen || shouldRefreshForInactiveScreen) {
			blockEntity.lastListingTick = currentTime;
			blockEntity.requestMarketData();
		}
	}

	// Screen

	public void openMarketScreen(World world, PlayerEntity player) {
		var client = MinecraftClient.getInstance();
		this.marketScreen = new MarketScreen();
		this.marketScreen.delegate = this;
		client.setScreen(marketScreen);

		sendStateSync(MarketBlockStateSyncReason.INTERACTION_START);
		requestMarketData();
	}

	public void onScreenUpdate() {
		if (this.marketScreen == null) {
			Commercialize.LOGGER.warn("Can not process market screen update, missing screen reference.");
			return;
		}

		requestMarketData();
		sendStateSync(MarketBlockStateSyncReason.UPDATE);
	}

	public void onScreenClose() {
		sendStateSync(MarketBlockStateSyncReason.INTERACTION_END);
	}

	private void updateMarketScreen() {
		if (this.marketScreen == null) {
			return;
		}

		var lastListingHash = this.lastListingHash;
		var currentListingHash = this.state.viewPropertiesHashCode();

		this.marketScreen.updateDisplay();

		this.lastListingHash = currentListingHash;

		if (lastListingHash != currentListingHash) {
			this.marketScreen.resetOfferScrollView();
		}
	}

}
