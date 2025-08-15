package net.saint.commercialize.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.market.MarketBlockEntityState;
import net.saint.commercialize.block.market.MarketBlockStateSyncReason;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;

public final class MarketC2SStateSyncMessage {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_c2s_state_sync");

	// Properties

	public BlockPos position;
	public MarketBlockStateSyncReason reason;
	public MarketBlockEntityState state;

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeBlockPos(position);
		buffer.writeEnumConstant(reason);
		buffer.writeString(state.searchTerm, 255);
		buffer.writeEnumConstant(state.sortMode);
		buffer.writeEnumConstant(state.sortOrder);
		buffer.writeEnumConstant(state.filterMode);
		buffer.writeEnumConstant(state.paymentMethod);
	}

	// Decoding

	public static MarketC2SStateSyncMessage decodeFromBuffer(PacketByteBuf buffer) {
		var message = new MarketC2SStateSyncMessage();
		message.state = new MarketBlockEntityState();

		message.position = buffer.readBlockPos();
		message.reason = buffer.readEnumConstant(MarketBlockStateSyncReason.class);
		message.state.searchTerm = buffer.readString(255);
		message.state.sortMode = buffer.readEnumConstant(OfferSortMode.class);
		message.state.sortOrder = buffer.readEnumConstant(OfferSortOrder.class);
		message.state.filterMode = buffer.readEnumConstant(OfferFilterMode.class);
		message.state.paymentMethod = buffer.readEnumConstant(PaymentMethod.class);

		return message;
	}

}
