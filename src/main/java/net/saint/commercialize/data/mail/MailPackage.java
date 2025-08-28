package net.saint.commercialize.data.mail;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.saint.commercialize.mixinlogic.PackageItemMixinLogic;

public final class MailPackage {

	// Configuration

	public static final PackageItemMixinLogic PACKAGE_ITEM = (PackageItemMixinLogic) Registries.ITEM
			.get(PackageItemMixinLogic.PACKAGE_ITEM_ID);

	// Creation

	public static ItemStack create(DefaultedList<ItemStack> items, Text message, Text sender) {
		return PACKAGE_ITEM.commercialize$create(items, message, sender);
	}

}
