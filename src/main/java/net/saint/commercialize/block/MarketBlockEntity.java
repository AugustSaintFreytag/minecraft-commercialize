package net.saint.commercialize.block;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.market.MarketManager;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.network.MarketC2SQueryMessage;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.screen.market.MarketScreen;

public class MarketBlockEntity extends BlockEntity {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_block_entity");

	// Properties

	private MarketManager marketManager = new MarketManager();

	public MarketScreen marketScreen;

	public String searchTerm = "";
	public OfferSortMode sortMode = OfferSortMode.ITEM_NAME;
	public OfferSortOrder sortOrder = OfferSortOrder.ASCENDING;
	public OfferFilterMode filterMode = OfferFilterMode.ALL;
	public PaymentMethod paymentMethod = PaymentMethod.INVENTORY;

	// Init

	public MarketBlockEntity(BlockPos position, BlockState state) {
		super(ModBlocks.MARKET_BLOCK_ENTITY, position, state);
	}

	// NBT

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);

		this.searchTerm = nbt.getString("searchTerm");
		this.sortMode = ifPresentAsString(nbt, "sortMode", value -> OfferSortMode.valueOf(value), OfferSortMode.ITEM_NAME);
		this.sortOrder = ifPresentAsString(nbt, "sortOrder", OfferSortOrder::valueOf, OfferSortOrder.ASCENDING);
		this.filterMode = ifPresentAsString(nbt, "filterMode", OfferFilterMode::valueOf, OfferFilterMode.ALL);
		this.paymentMethod = ifPresentAsString(nbt, "paymentMethod", PaymentMethod::valueOf, PaymentMethod.INVENTORY);
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);

		nbt.putString("searchTerm", this.searchTerm);
		nbt.putString("sortMode", this.sortMode.name());
		nbt.putString("sortOrder", this.sortOrder.name());
		nbt.putString("filterMode", this.filterMode.name());
		nbt.putString("paymentMethod", this.paymentMethod.name());
	}

	// Networking

	public void receiveServerMessage(MarketS2CListMessage message) {
		marketManager.clearOffers();
		marketManager.addOffers(message.offers);
		marketManager.setOffersAreCapped(message.isCapped);

		Commercialize.LOGGER.info("Received market data for market block entity at pos '{}' from server: {} offer(s) available.",
				this.getPos().toShortString(), marketManager.getOffers().count());

		updateMarketScreen();
	}

	// Screen

	public void openMarketScreen(World world, PlayerEntity player) {
		if (!world.isClient()) {
			return;
		}

		var client = MinecraftClient.getInstance();

		this.marketScreen = new MarketScreen();

		this.marketScreen.onUpdate = this::onMarketScreenUpdate;
		this.marketScreen.searchTerm = searchTerm;
		this.marketScreen.sortMode = sortMode;
		this.marketScreen.sortOrder = sortOrder;
		this.marketScreen.filterMode = filterMode;
		this.marketScreen.paymentMethod = paymentMethod;

		marketScreen.offers = marketManager.getOffers().toList();
		marketScreen.offersAreCapped = marketManager.offersAreCapped();

		client.setScreen(marketScreen);

		requestMarketData();
	}

	private void onMarketScreenUpdate() {
		if (this.marketScreen == null) {
			Commercialize.LOGGER.warn("Can not process market screen update, missing screen reference.");
			return;
		}

		this.searchTerm = this.marketScreen.searchTerm;
		this.filterMode = this.marketScreen.filterMode;
		this.sortMode = this.marketScreen.sortMode;
		this.sortOrder = this.marketScreen.sortOrder;
		this.paymentMethod = this.marketScreen.paymentMethod;

		this.requestMarketData();
	}

	private void updateMarketScreen() {
		if (this.marketScreen == null) {
			Commercialize.LOGGER.warn(
					"Can not update market screen, missing screen reference. Update likely requested for inactive block or incorrect receiver.");
			return;
		}

		this.marketScreen.offers = marketManager.getOffers().toList();
		this.marketScreen.updateDisplay();
	}

	// Networking

	public void requestMarketData() {
		var message = new MarketC2SQueryMessage();

		message.position = this.getPos();
		message.searchTerm = searchTerm;
		message.sortMode = sortMode;
		message.sortOrder = sortOrder;
		message.filterMode = filterMode;

		var buffer = PacketByteBufs.create();
		message.encodeToBuffer(buffer);

		ClientPlayNetworking.send(MarketC2SQueryMessage.ID, buffer);
	}

}
