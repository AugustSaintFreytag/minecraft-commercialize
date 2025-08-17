package net.saint.commercialize.screen.selling;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SellingScreenState {

	// Properties

	public ItemStack selectedItem = ItemStack.EMPTY;

	public int offerPrice = 0;

	public long offerDuration = 0;

	public OfferPostStrategy offerPostStrategy = OfferPostStrategy.AS_STACK;

	// NBT

	public NbtCompound toNbtCompound() {
		var nbt = new NbtCompound();

		nbt.put("selected_item", selectedItem.writeNbt(new NbtCompound()));
		nbt.putInt("offer_price", this.offerPrice);
		nbt.putLong("offer_duration", this.offerDuration);
		nbt.putString("offer_post_strategy", this.offerPostStrategy.name());

		return nbt;
	}

	public void readNbtCompound(NbtCompound nbt) {
		this.selectedItem = ItemStack.fromNbt(nbt.getCompound("selected_item"));
		this.offerPrice = nbt.getInt("offer_price");
		this.offerDuration = nbt.getLong("offer_duration");
		this.offerPostStrategy = OfferPostStrategy.valueOf(nbt.getString("offer_post_strategy"));
	}

}
