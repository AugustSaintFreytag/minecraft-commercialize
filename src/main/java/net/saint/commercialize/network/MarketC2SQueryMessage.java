package net.saint.commercialize.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;

public final class MarketC2SQueryMessage {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_c2s_query");

	// Properties

	public BlockPos position;
	public OfferSortMode sortMode;
	public OfferSortOrder sortOrder;
	public OfferFilterMode filterMode;

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeBlockPos(position);
		buffer.writeEnumConstant(sortMode);
		buffer.writeEnumConstant(sortOrder);
		buffer.writeEnumConstant(filterMode);
	}

	// Decoding

	public static MarketC2SQueryMessage decodeFromBuffer(PacketByteBuf buffer) {
		var message = new MarketC2SQueryMessage();

		message.position = buffer.readBlockPos();
		message.sortMode = buffer.readEnumConstant(OfferSortMode.class);
		message.sortOrder = buffer.readEnumConstant(OfferSortOrder.class);
		message.filterMode = buffer.readEnumConstant(OfferFilterMode.class);

		return message;
	}

}
