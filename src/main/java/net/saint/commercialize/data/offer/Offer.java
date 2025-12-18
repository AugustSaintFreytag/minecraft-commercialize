package net.saint.commercialize.data.offer;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class Offer {

	// Configuration

	public static final UUID GENERATED_SELLER_ID = new UUID(0, 0);

	private static final String ID_NBT_KEY = "Id";
	private static final String IS_ACTIVE_NBT_KEY = "IsActive";
	private static final String IS_GENERATED_NBT_KEY = "IsGenerated";
	private static final String SELLER_ID_NBT_KEY = "SellerId";
	private static final String SELLER_NAME_NBT_KEY = "SellerName";
	private static final String TIMESTAMP_NBT_KEY = "Timestamp";
	private static final String DURATION_NBT_KEY = "Duration";
	private static final String STACK_NBT_KEY = "Stack";
	private static final String PRICE_NBT_KEY = "Price";
	private static final String FEES_NBT_KEY = "Fees";

	// Properties

	public UUID id;

	public boolean isActive;
	public boolean isGenerated;

	public UUID sellerId;
	public String sellerName;

	public long timestamp;
	public long duration;

	public ItemStack stack;
	public int price;
	public int fees;

	// Decoding

	public static Offer fromBuffer(PacketByteBuf buffer) {
		var offer = new Offer();

		offer.id = buffer.readUuid();
		offer.isActive = buffer.readBoolean();
		offer.isGenerated = buffer.readBoolean();
		offer.sellerId = buffer.readUuid();
		offer.sellerName = buffer.readString(32767);
		offer.timestamp = buffer.readLong();
		offer.duration = buffer.readLong();
		offer.stack = buffer.readItemStack();
		offer.price = buffer.readInt();
		offer.fees = buffer.readInt();

		return offer;
	}

	public static Offer fromNBT(NbtCompound nbt) {
		var offer = new Offer();

		offer.id = nbt.getUuid(ID_NBT_KEY);
		offer.isActive = nbt.getBoolean(IS_ACTIVE_NBT_KEY);
		offer.isGenerated = nbt.getBoolean(IS_GENERATED_NBT_KEY);
		offer.sellerId = nbt.getUuid(SELLER_ID_NBT_KEY);
		offer.sellerName = nbt.getString(SELLER_NAME_NBT_KEY);
		offer.timestamp = nbt.getLong(TIMESTAMP_NBT_KEY);
		offer.duration = nbt.getLong(DURATION_NBT_KEY);
		offer.stack = ItemStack.fromNbt(nbt.getCompound(STACK_NBT_KEY));
		offer.price = nbt.getInt(PRICE_NBT_KEY);
		offer.fees = nbt.getInt(FEES_NBT_KEY);

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
		buffer.writeLong(duration);
		buffer.writeItemStack(stack);
		buffer.writeInt(price);
		buffer.writeInt(fees);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putUuid(ID_NBT_KEY, id);
		nbt.putBoolean(IS_ACTIVE_NBT_KEY, isActive);
		nbt.putBoolean(IS_GENERATED_NBT_KEY, isGenerated);
		nbt.putUuid(SELLER_ID_NBT_KEY, sellerId);
		nbt.putString(SELLER_NAME_NBT_KEY, sellerName);
		nbt.putLong(TIMESTAMP_NBT_KEY, timestamp);
		nbt.putLong(DURATION_NBT_KEY, duration);
		nbt.put(STACK_NBT_KEY, stack.writeNbt(new NbtCompound()));
		nbt.putInt(PRICE_NBT_KEY, price);
		nbt.putInt(FEES_NBT_KEY, fees);

		return nbt;
	}

}