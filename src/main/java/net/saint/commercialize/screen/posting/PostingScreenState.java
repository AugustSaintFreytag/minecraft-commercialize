package net.saint.commercialize.screen.posting;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.saint.commercialize.data.text.TimePreset;

public class PostingScreenState {

	// Properties

	public ItemStack stack = ItemStack.EMPTY;

	public int price = 0;

	public long duration = TimePreset.THREE_DAYS;

	public OfferPostStrategy postStrategy = OfferPostStrategy.AS_STACK;

	// NBT

	public NbtCompound toNbtCompound() {
		var nbt = new NbtCompound();

		nbt.put("selected_item", stack.writeNbt(new NbtCompound()));
		nbt.putInt("offer_price", this.price);
		nbt.putLong("offer_duration", this.duration);
		nbt.putString("offer_post_strategy", this.postStrategy.name());

		return nbt;
	}

	public void readNbtCompound(NbtCompound nbt) {
		this.stack = ItemStack.fromNbt(nbt.getCompound("selected_item"));
		this.price = nbt.getInt("offer_price");
		this.duration = nbt.getLong("offer_duration");
		this.postStrategy = OfferPostStrategy.valueOf(nbt.getString("offer_post_strategy"));
	}

}
