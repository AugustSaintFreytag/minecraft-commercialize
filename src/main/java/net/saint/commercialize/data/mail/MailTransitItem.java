package net.saint.commercialize.data.mail;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

public class MailTransitItem {

	// Properties

	public final long timeDispatched;
	public final UUID recipient;
	public final DefaultedList<ItemStack> items;

	// Init

	public MailTransitItem(long timeDispatched, UUID recipient, DefaultedList<ItemStack> items) {
		this.timeDispatched = timeDispatched;
		this.recipient = recipient;
		this.items = items;
	}

	// NBT

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putLong("timeDispatched", timeDispatched);
		nbt.putUuid("recipient", recipient);

		var itemsNbt = new NbtList();

		for (int i = 0; i < items.size(); i++) {
			var itemStack = items.get(i);
			var itemStackNbt = new NbtCompound();
			itemsNbt.add(itemStack.writeNbt(itemStackNbt));
		}

		nbt.put("items", itemsNbt);
		return nbt;
	}

	public static MailTransitItem fromNbt(NbtCompound nbt) {
		var timeDispatched = nbt.getLong("timeDispatched");
		var recipient = nbt.getUuid("recipient");

		var itemsNbt = nbt.getList("items", 10); // 10 = NbtCompound
		var items = DefaultedList.ofSize(itemsNbt.size(), ItemStack.EMPTY);

		for (int i = 0; i < itemsNbt.size(); i++) {
			items.set(i, ItemStack.fromNbt(itemsNbt.getCompound(i)));
		}

		return new MailTransitItem(timeDispatched, recipient, items);
	}

}