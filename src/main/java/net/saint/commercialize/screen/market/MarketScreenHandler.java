package net.saint.commercialize.screen.market;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.market.MarketBlockScreenDelegateHandler;
import net.saint.commercialize.block.market.MarketBlockStateSyncReason;
import net.saint.commercialize.block.market.MarketScreenState;
import net.saint.commercialize.network.MarketC2SOrderMessage;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketC2SStateSyncMessage;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.network.MarketS2COrderMessage;

public class MarketScreenHandler implements MarketBlockScreenDelegateHandler {

	// References

	public BlockPos position;
	public PlayerEntity player;
	public MarketScreen marketScreen;

	// Properties

	private MarketScreenState state = new MarketScreenState();

	protected long lastListingTick = 0;
	protected int lastListingHash = 0;

	// Init

	public MarketScreenHandler() {
	}

	// Access

	public MarketScreenState getState() {
		return state;
	}

	public void setState(MarketScreenState state) {
		this.state = state;
	}

	// Networking

	public void receiveListMessage(MarketS2CListMessage message) {
		state.balance = message.balance;
		state.cardOwner = message.cardOwner;

		state.marketOffers.clearOffers();
		state.marketOffers.addOffers(message.offers);
		state.marketOffers.setOffersAreCapped(message.isCapped);

		lastListingTick = this.player.getWorld().getTimeOfDay();
		updateMarketScreen();
	}

	public void receiveOrderMessage(MarketS2COrderMessage message) {
		switch (message.result) {
			case INSUFFICIENT_FUNDS:
				break;
			case INVIABLE_DELIVERY:
				break;
			case INVIABLE_OFFERS:
				break;
			case INVIABLE_PAYMENT_METHOD:
				break;
			case FAILURE:
				break;
			case SUCCESS:
				state.cartOffers.clearOffers();

				requestMarketData();
				updateMarketScreen();
				break;
		}
	}

	public void sendStateSync(MarketBlockStateSyncReason reason) {
		var message = new MarketC2SStateSyncMessage();
		message.position = this.position;
		message.reason = reason;
		message.state = this.state;

		var buffer = PacketByteBufs.create();
		message.encodeToBuffer(buffer);

		ClientPlayNetworking.send(MarketC2SStateSyncMessage.ID, buffer);
	}

	public void requestMarketData() {
		var message = new MarketC2SQueryMessage();

		message.position = this.position;
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

		message.position = this.position;
		message.offers = state.cartOffers.getOffers().map(offer -> offer.id).toList();
		message.paymentMethod = state.paymentMethod;

		var buffer = PacketByteBufs.create();
		message.encodeToBuffer(buffer);

		ClientPlayNetworking.send(MarketC2SOrderMessage.ID, buffer);
	}

	// Ticking

	public void tick() {
		var world = this.player.getWorld();
		var currentTime = world.getTimeOfDay();
		var timeSinceLastListing = currentTime - lastListingTick;

		var isScreenActive = marketScreen != null;
		var shouldRefreshForActiveScreen = isScreenActive && timeSinceLastListing > Commercialize.CONFIG.listingRefreshInterval;
		var shouldRefreshForInactiveScreen = !isScreenActive && Commercialize.CONFIG.listingRefreshIntervalWhenInactive != -1
				&& timeSinceLastListing > Commercialize.CONFIG.listingRefreshIntervalWhenInactive;

		if (shouldRefreshForActiveScreen || shouldRefreshForInactiveScreen) {
			lastListingTick = currentTime;
			requestMarketData();
		}
	}

	// Screen

	public void openScreen() {
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
