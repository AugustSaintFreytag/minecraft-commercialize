package net.saint.commercialize.data.offer;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class Offer {

	// Properties

	public UUID id;

	public boolean isActive;
	public boolean isGenerated;

	@Nullable
	public UUID sellerId;

	@Nullable
	public String sellerName;

	public long timestamp;
	public int duration;

	public ItemStack stack;
	public int price;

	// Decoding

	public static Offer decodeFromBuffer(PacketByteBuf buffer) {
		var offer = new Offer();

		offer.id = buffer.readUuid();
		offer.isActive = buffer.readBoolean();
		offer.isGenerated = buffer.readBoolean();
		offer.sellerId = buffer.readUuid();
		offer.sellerName = buffer.readString(32767);
		offer.timestamp = buffer.readLong();
		offer.duration = buffer.readInt();
		offer.stack = buffer.readItemStack();
		offer.price = buffer.readInt();

		return offer;
	}

	public static Offer decodeFromNBT(NbtCompound nbt) {
		var offer = new Offer();

		offer.id = nbt.getUuid("id");
		offer.isActive = nbt.getBoolean("isActive");
		offer.isGenerated = nbt.getBoolean("isGenerated");

		if (nbt.contains("sellerId")) {
			offer.sellerId = nbt.getUuid("sellerId");
		}

		if (nbt.contains("sellerName")) {
			offer.sellerName = nbt.getString("sellerName");
		}

		offer.timestamp = nbt.getLong("timestamp");
		offer.duration = nbt.getInt("duration");
		offer.stack = ItemStack.fromNbt(nbt.getCompound("stack"));
		offer.price = nbt.getInt("price");

		return offer;
	}

	// Encoding

	public void encodeToBuffer(PacketByteBuf buffer) {
		buffer.writeUuid(id);
		buffer.writeBoolean(isActive);
		buffer.writeBoolean(isGenerated);

		if (sellerId != null) {
			buffer.writeUuid(sellerId);
		}

		if (sellerName != null) {
			buffer.writeString(sellerName);
		}

		buffer.writeLong(timestamp);
		buffer.writeInt(duration);
		buffer.writeItemStack(stack);
		buffer.writeInt(price);
	}

	public void encodeToNBT(NbtCompound nbt) {
		nbt.putUuid("id", id);
		nbt.putBoolean("isActive", isActive);
		nbt.putBoolean("isGenerated", isGenerated);

		if (sellerId != null) {
			nbt.putUuid("sellerId", sellerId);
		}

		if (sellerName != null) {
			nbt.putString("sellerName", sellerName);
		}

		nbt.putLong("timestamp", timestamp);
		nbt.putInt("duration", duration);
		nbt.put("stack", stack.writeNbt(new NbtCompound()));
		nbt.putInt("price", price);
	}

}