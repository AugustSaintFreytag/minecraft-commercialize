package net.saint.commercialize.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.saint.commercialize.item.LetterItem;

public final class ModItems {

	// Items

	public static Item LETTER_ITEM;

	// Init

	public static void initialize() {
		LETTER_ITEM = Registry.register(Registries.ITEM, LetterItem.ID, new LetterItem(new FabricItemSettings()));
	}

}
