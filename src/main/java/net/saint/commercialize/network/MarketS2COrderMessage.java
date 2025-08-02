package net.saint.commercialize.network;

import java.util.List;
import java.util.UUID;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;

public final class MarketS2COrderMessage {

	// Library

	public static enum Result {
		SUCCESS, INSUFFICIENT_FUNDS, INVIABLE_OFFERS, INVIABLE_DELIVERY, INVIABLE_PAYMENT_METHOD, FAILURE
	}

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_s2c_order");

	// Properties

	public BlockPos position;
	public List<UUID> offers;
	public Result result;

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeBlockPos(position);
		buffer.writeCollection(offers, (itemBuffer, id) -> itemBuffer.writeUuid(id));
		buffer.writeEnumConstant(result);
	}

	// Decoding

	public static MarketS2COrderMessage decodeFromBuffer(PacketByteBuf buffer) {
		var message = new MarketS2COrderMessage();

		message.position = buffer.readBlockPos();
		message.offers = buffer.readList(itemBuffer -> itemBuffer.readUuid());
		message.result = buffer.readEnumConstant(Result.class);

		return message;
	}

}
