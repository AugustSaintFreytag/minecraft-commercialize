package net.saint.commercialize.network;

import java.util.List;

import net.minecraft.network.PacketByteBuf;
import net.saint.commercialize.data.offer.Offer;

public final class MarketS2CListMessage {

	// Configuration

	public static final String ID = "market_s2c_list";

	// Properties

	public int index;
	public List<Offer> offers;

	// Decoding

	public static MarketS2CListMessage decodeFromBuffer(PacketByteBuf buffer) {
		var message = new MarketS2CListMessage();

		message.index = buffer.readInt();
		message.offers = buffer.readList(Offer::decodeFromBuffer);

		return message;
	}

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeInt(index);
		buffer.writeCollection(offers, (localBuffer, offer) -> offer.encodeToBuffer(localBuffer));
	}

}
