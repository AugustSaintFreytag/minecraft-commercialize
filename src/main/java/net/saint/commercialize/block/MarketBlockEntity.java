package net.saint.commercialize.block;

import static net.saint.commercialize.util.Values.ifPresentAsString;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.init.ModSounds;
import net.saint.commercialize.network.MarketC2SOrderMessage;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.network.MarketS2COrderMessage;
import net.saint.commercialize.screen.market.MarketScreen;
import net.saint.commercialize.screen.market.MarketScreenUtil;
import net.saint.commercialize.util.LocalizationUtil;
import net.saint.commercialize.util.NumericFormattingUtil;

public class MarketBlockEntity extends BlockEntity implements MarketBlockEntityScreenHandler {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_block_entity");

	// Properties

	private MarketBlockEntityScreenState state = new MarketBlockEntityScreenState();

	private int lastMarketHash = 0;

	private MarketScreen marketScreen;

	// Init

	public MarketBlockEntity(BlockPos position, BlockState state) {
		super(ModBlocks.MARKET_BLOCK_ENTITY, position, state);
	}

	// Access

	public MarketBlockEntityScreenState getState() {
		return state;
	}

	// NBT

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);

		state.searchTerm = nbt.getString("searchTerm");
		state.sortMode = ifPresentAsString(nbt, "sortMode", value -> OfferSortMode.valueOf(value), OfferSortMode.ITEM_NAME);
		state.sortOrder = ifPresentAsString(nbt, "sortOrder", OfferSortOrder::valueOf, OfferSortOrder.ASCENDING);
		state.filterMode = ifPresentAsString(nbt, "filterMode", OfferFilterMode::valueOf, OfferFilterMode.ALL);
		state.paymentMethod = ifPresentAsString(nbt, "paymentMethod", PaymentMethod::valueOf, PaymentMethod.INVENTORY);
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);

		nbt.putString("searchTerm", state.searchTerm);
		nbt.putString("sortMode", state.sortMode.name());
		nbt.putString("sortOrder", state.sortOrder.name());
		nbt.putString("filterMode", state.filterMode.name());
		nbt.putString("paymentMethod", state.paymentMethod.name());
	}

	// Networking

	public void receiveListMessage(MarketS2CListMessage message) {
		state.marketOffers.clearOffers();
		state.marketOffers.addOffers(message.offers);
		state.marketOffers.setOffersAreCapped(message.isCapped);

		Commercialize.LOGGER.info("Received market data for market block entity at pos '{}' from server: {} offer(s) available.",
				this.getPos().toShortString(), state.marketOffers.getOffers().count());

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
		case INVIABLE_OFFERS: {
			var displayText = LocalizationUtil.localizedText("gui", "market.order_error_inviable_offers");
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
			var formattedTotal = NumericFormattingUtil.formatCurrency(getCartTotal());
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

	// Screen

	public void openMarketScreen(World world, PlayerEntity player) {
		if (!world.isClient()) {
			return;
		}

		var client = MinecraftClient.getInstance();
		this.marketScreen = new MarketScreen();
		this.marketScreen.delegate = this;
		client.setScreen(marketScreen);

		requestMarketData();
	}

	public void onMarketScreenUpdate() {
		if (this.marketScreen == null) {
			Commercialize.LOGGER.warn("Can not process market screen update, missing screen reference.");
			return;
		}

		this.requestMarketData();
	}

	private void updateMarketScreen() {
		if (this.marketScreen == null) {
			Commercialize.LOGGER.warn(
					"Can not update market screen, missing screen reference. Update likely requested for inactive block or incorrect receiver.");
			return;
		}

		var lastMarketHash = this.lastMarketHash;
		var currentMarketHash = this.state.marketOffers.hashCode();

		this.marketScreen.updateDisplay();

		this.lastMarketHash = currentMarketHash;

		if (lastMarketHash != currentMarketHash) {
			this.marketScreen.resetOfferScrollView();
		}
	}

	// Networking

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

}
