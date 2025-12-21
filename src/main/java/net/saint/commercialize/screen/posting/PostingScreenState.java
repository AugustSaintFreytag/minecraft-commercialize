package net.saint.commercialize.screen.posting;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.saint.commercialize.data.text.TimePreset;

public class PostingScreenState {

	// Configuration

	private static final String SELECTED_ITEM_NBT_KEY = "SelectedItem";
	private static final String OFFER_PRICE_NBT_KEY = "OfferPrice";
	private static final String OFFER_DURATION_NBT_KEY = "OfferDuration";
	private static final String OFFER_POST_STRATEGY_NBT_KEY = "OfferPostStrategy";

	// Properties

	public ItemStack stack = ItemStack.EMPTY;

	public int price = 0;

	public long duration = TimePreset.threeDays();

	public OfferPostStrategy postStrategy = OfferPostStrategy.AS_STACK;

	// Properties (Transient)

	public int balance = 0;

	// NBT

	public NbtCompound toNbtCompound() {
		var nbt = new NbtCompound();

		nbt.put(SELECTED_ITEM_NBT_KEY, stack.writeNbt(new NbtCompound()));
		nbt.putInt(OFFER_PRICE_NBT_KEY, this.price);
		nbt.putLong(OFFER_DURATION_NBT_KEY, this.duration);
		nbt.putString(OFFER_POST_STRATEGY_NBT_KEY, this.postStrategy.name());

		return nbt;
	}

	public void readNbtCompound(NbtCompound nbt) {
		this.stack = ItemStack.fromNbt(nbt.getCompound(SELECTED_ITEM_NBT_KEY));
		this.price = nbt.getInt(OFFER_PRICE_NBT_KEY);
		this.duration = nbt.getLong(OFFER_DURATION_NBT_KEY);
		this.postStrategy = OfferPostStrategy.valueOf(nbt.getString(OFFER_POST_STRATEGY_NBT_KEY));
	}

}
