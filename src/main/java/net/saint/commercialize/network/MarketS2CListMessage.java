package net.saint.commercialize.network;

import java.util.List;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;

public final class MarketS2CListMessage {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_s2c_list");

	// Properties

	public BlockPos position;
	public List<Offer> offers;
	public boolean isCapped;

	// Decoding

	public static MarketS2CListMessage decodeFromBuffer(PacketByteBuf buffer) {
		var message = new MarketS2CListMessage();

		message.position = buffer.readBlockPos();
		message.offers = buffer.readList(Offer::fromBuffer);
		message.isCapped = buffer.readBoolean();

		return message;
	}

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeBlockPos(position);
		buffer.writeCollection(offers, (localBuffer, offer) -> offer.toBuffer(localBuffer));
		buffer.writeBoolean(isCapped);
	}

}
