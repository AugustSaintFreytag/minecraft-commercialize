package net.saint.commercialize.data.mail;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class MailTransitItem {

	// Properties

	public final long timeDispatched;
	public final UUID recipient;
	public final ItemStack item;

	// Init

	public MailTransitItem(long timeDispatched, UUID recipient, ItemStack item) {
		this.timeDispatched = timeDispatched;
		this.recipient = recipient;
		this.item = item;
	}

	// NBT

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putLong("timeDispatched", timeDispatched);
		nbt.putUuid("recipient", recipient);

		var itemNbt = new NbtCompound();
		nbt.put("item", item.writeNbt(itemNbt));

		return nbt;
	}

	public static MailTransitItem fromNbt(NbtCompound nbt) {
		var timeDispatched = nbt.getLong("timeDispatched");
		var recipient = nbt.getUuid("recipient");
		var item = ItemStack.fromNbt(nbt.getCompound("item"));

		return new MailTransitItem(timeDispatched, recipient, item);
	}

}
