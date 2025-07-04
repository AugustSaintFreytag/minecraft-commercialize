package net.saint.commercialize.network;

import java.util.List;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;

public final class MarketS2CListMessage {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_s2c_list");

	// Properties

	public List<Offer> offers;
	public boolean isCapped;

	// Decoding

	public static MarketS2CListMessage decodeFromBuffer(PacketByteBuf buffer) {
		var message = new MarketS2CListMessage();

		message.offers = buffer.readList(Offer::decodeFromBuffer);
		message.isCapped = buffer.readBoolean();

		return message;
	}

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeCollection(offers, (localBuffer, offer) -> offer.encodeToBuffer(localBuffer));
		buffer.writeBoolean(isCapped);
	}

}
