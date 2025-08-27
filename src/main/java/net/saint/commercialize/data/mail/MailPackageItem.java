package net.saint.commercialize.data.mail;

import java.util.function.Consumer;

import com.mrcrayfish.furniture.refurbished.item.PackageItem;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.saint.commercialize.Commercialize;

public class MailPackageItem extends PackageItem {

	// Configuration

	public static final Identifier IDENTIFIER = new Identifier(Commercialize.MOD_ID, "package");

	private static final String HOST_MOD_ID = "refurbished_furniture";
	private static final Identifier PACKAGE_ITEM_IDENTIFIER = new Identifier(HOST_MOD_ID, "package");

	private static final Item PACKAGE_ITEM = Registries.ITEM.get(PACKAGE_ITEM_IDENTIFIER);

	public static final String MESSAGE_NBT_KEY = "messageText";
	public static final String SENDER_NBT_KEY = "senderText";

	// Init

	public MailPackageItem(Settings properties) {
		super(properties);
	}

	public static ItemStack create(DefaultedList<ItemStack> itemStackList, Text message, Text sender) {
		var packageItemStack = new ItemStack(PACKAGE_ITEM);
		var packageItemNbt = packageItemStack.getOrCreateNbt();

		Inventories.writeNbt(packageItemNbt, itemStackList);

		var encodedMessage = Text.Serializer.toJson(message);
		var encodedSender = Text.Serializer.toJson(sender);

		packageItemNbt.putString(MESSAGE_NBT_KEY, encodedMessage);
		packageItemNbt.putString(SENDER_NBT_KEY, encodedSender);

		return packageItemStack;
	}

	// Utility

	private static void withTextFromNBT(ItemStack stack, String key, Consumer<Text> block) {
		if (!stack.hasNbt() || !stack.getNbt().contains(key)) {
			return;
		}

		var encodedText = stack.getNbt().getString(key);
		var text = Text.Serializer.fromJson(encodedText);

		if (text == null) {
			return;
		}

		block.accept(text);
	}

}
