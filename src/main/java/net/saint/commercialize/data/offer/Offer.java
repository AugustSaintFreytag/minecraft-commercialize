package net.saint.commercialize.data.offer;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class Offer {

	public static final UUID GENERATED_SELLER_ID = new UUID(0, 0);

	// Properties

	public UUID id;

	public boolean isActive;
	public boolean isGenerated;

	public UUID sellerId;
	public String sellerName;

	public long timestamp;
	public int duration;

	public ItemStack stack;
	public int price;

	// Decoding

	public static Offer fromBuffer(PacketByteBuf buffer) {
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

	public static Offer fromNBT(NbtCompound nbt) {
		var offer = new Offer();

		offer.id = nbt.getUuid("id");
		offer.isActive = nbt.getBoolean("isActive");
		offer.isGenerated = nbt.getBoolean("isGenerated");
		offer.sellerId = nbt.getUuid("sellerId");
		offer.sellerName = nbt.getString("sellerName");
		offer.timestamp = nbt.getLong("timestamp");
		offer.duration = nbt.getInt("duration");
		offer.stack = ItemStack.fromNbt(nbt.getCompound("stack"));
		offer.price = nbt.getInt("price");

		return offer;
	}

	// Encoding

	public void toBuffer(PacketByteBuf buffer) {
		buffer.writeUuid(id);
		buffer.writeBoolean(isActive);
		buffer.writeBoolean(isGenerated);
		buffer.writeUuid(sellerId);
		buffer.writeString(sellerName);
		buffer.writeLong(timestamp);
		buffer.writeInt(duration);
		buffer.writeItemStack(stack);
		buffer.writeInt(price);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putUuid("id", id);
		nbt.putBoolean("isActive", isActive);
		nbt.putBoolean("isGenerated", isGenerated);
		nbt.putUuid("sellerId", sellerId);
		nbt.putString("sellerName", sellerName);
		nbt.putLong("timestamp", timestamp);
		nbt.putInt("duration", duration);
		nbt.put("stack", stack.writeNbt(new NbtCompound()));
		nbt.putInt("price", price);

		return nbt;
	}

}