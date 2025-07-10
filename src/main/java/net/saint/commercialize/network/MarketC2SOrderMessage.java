package net.saint.commercialize.network;

import java.util.List;
import java.util.UUID;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.payment.PaymentMethod;

public final class MarketC2SOrderMessage {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "market_c2s_order");

	// Properties

	public BlockPos position;
	public List<UUID> offers;
	public PaymentMethod paymentMethod;

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeBlockPos(position);
		buffer.writeCollection(offers, (itemBuffer, id) -> itemBuffer.writeUuid(id));
		buffer.writeEnumConstant(paymentMethod);
	}

	// Decoding

	public static MarketC2SOrderMessage decodeFromBuffer(PacketByteBuf buffer) {
		var message = new MarketC2SOrderMessage();

		message.position = buffer.readBlockPos();
		message.offers = buffer.readList(itemBuffer -> itemBuffer.readUuid());
		message.paymentMethod = buffer.readEnumConstant(PaymentMethod.class);

		return message;
	}

}
