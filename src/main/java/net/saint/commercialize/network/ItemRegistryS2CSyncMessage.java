package net.saint.commercialize.network;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;

public class ItemRegistryS2CSyncMessage {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "item_registry_s2c_sync");

	// Properties

	public Map<Identifier, Integer> valueByItem;

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeInt(valueByItem.size());
		
		for (var entry : valueByItem.entrySet()) {
			buffer.writeIdentifier(entry.getKey());
			buffer.writeInt(entry.getValue());
		}
	}

	// Decoding

	public static ItemRegistryS2CSyncMessage decodeFromBuffer(PacketByteBuf buffer) {
		var message = new ItemRegistryS2CSyncMessage();
		message.valueByItem = new HashMap<>();

		var count = buffer.readInt();
		for (var i = 0; i < count; i++) {
			var item = buffer.readIdentifier();
			var value = buffer.readInt();

			message.valueByItem.put(item, value);
		}

		return message;
	}
	
}
