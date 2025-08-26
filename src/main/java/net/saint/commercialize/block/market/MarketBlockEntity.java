package net.saint.commercialize.block.market;

import static net.saint.commercialize.util.Values.returnIfPresentAsString;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.init.ModBlockEntities;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.network.MarketS2COrderMessage;
import net.saint.commercialize.screen.market.MarketScreenHandler;

public class MarketBlockEntity extends BlockEntity {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_block_entity");

	// Properties

	private MarketScreenState state = new MarketScreenState();

	private MarketScreenHandler screenHandler;

	// Init

	public MarketBlockEntity(BlockPos position, BlockState state) {
		super(ModBlockEntities.MARKET_BLOCK_ENTITY, position, state);
	}

	// Access

	public MarketScreenState getState() {
		return state;
	}

	public void setState(MarketScreenState state) {
		this.state = state;
		this.markDirty();
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
		if (this.screenHandler == null) {
			Commercialize.LOGGER.warn("Could not receive and handle server to client list message, no screen handler.");
			return;
		}

		this.screenHandler.receiveListMessage(message);
	}

	public void receiveOrderMessage(MarketS2COrderMessage message) {
		if (this.screenHandler == null) {
			Commercialize.LOGGER.warn("Could not receive and handle server to client order message, no screen handler.");
			return;
		}

		this.screenHandler.receiveOrderMessage(message);
	}

	// Ticking

	public static void tick(World world, BlockPos position, BlockState state, MarketBlockEntity blockEntity) {
		if (!world.isClient() || blockEntity.screenHandler == null) {
			return;
		}

		blockEntity.screenHandler.tick();
	}

	// Screen

	@Environment(EnvType.CLIENT)
	public void openScreen(World world, PlayerEntity player) {
		this.screenHandler = new MarketScreenHandler();
		this.screenHandler.position = this.getPos();
		this.screenHandler.player = player;

		this.screenHandler.openScreen();
	}

}
