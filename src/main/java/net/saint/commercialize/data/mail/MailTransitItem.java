package net.saint.commercialize.data.mail;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class MailTransitItem {

	// Configuration

	public static final String TIME_DISPATCHED_NBT_KEY = "TimeDispatched";
	public static final String TIME_LAST_DELIVERY_ATTEMPTED_NBT_KEY = "TimeLastDeliveryAttempted";
	public static final String NUMBER_OF_DELIVERY_ATTEMPTS_NBT_KEY = "NumberOfDeliveryAttempts";

	public static final String RECIPIENT_NBT_KEY = "Recipient";
	public static final String STACK_NBT_KEY = "Stack";

	// Properties

	public long timeDispatched;
	public long timeLastDeliveryAttempted;
	public int numberOfDeliveryAttempts = 0;

	public UUID recipient;
	public ItemStack stack = ItemStack.EMPTY;

	// Init

	public MailTransitItem(long timeDispatched, UUID recipient, ItemStack stack) {
		this.timeDispatched = timeDispatched;
		this.recipient = recipient;
		this.stack = stack;
	}

	// NBT

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putLong(MailTransitItem.TIME_DISPATCHED_NBT_KEY, timeDispatched);
		nbt.putLong(MailTransitItem.TIME_LAST_DELIVERY_ATTEMPTED_NBT_KEY, timeLastDeliveryAttempted);
		nbt.putInt(MailTransitItem.NUMBER_OF_DELIVERY_ATTEMPTS_NBT_KEY, numberOfDeliveryAttempts);
		nbt.putUuid(MailTransitItem.RECIPIENT_NBT_KEY, recipient);

		var itemNbt = new NbtCompound();
		nbt.put(MailTransitItem.STACK_NBT_KEY, stack.writeNbt(itemNbt));

		return nbt;
	}

	public static MailTransitItem fromNbt(NbtCompound nbt) {
		var timeDispatched = nbt.getLong(MailTransitItem.TIME_DISPATCHED_NBT_KEY);
		var timeLastDeliveryAttempted = nbt.getLong(MailTransitItem.TIME_LAST_DELIVERY_ATTEMPTED_NBT_KEY);
		var numberOfDeliveryAttempts = nbt.getInt(MailTransitItem.NUMBER_OF_DELIVERY_ATTEMPTS_NBT_KEY);
		var recipient = nbt.getUuid(MailTransitItem.RECIPIENT_NBT_KEY);
		var stack = ItemStack.fromNbt(nbt.getCompound(MailTransitItem.STACK_NBT_KEY));

		var item = new MailTransitItem(timeDispatched, recipient, stack);
		item.timeLastDeliveryAttempted = timeLastDeliveryAttempted;
		item.numberOfDeliveryAttempts = numberOfDeliveryAttempts;

		return item;
	}

}
