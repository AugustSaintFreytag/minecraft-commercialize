package net.saint.commercialize.db.common;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;

public final class ItemStackCodingUtil {

	// Item Stack Coding

	/**
	 * Encodes an `ItemStack` into a JSON string for database storage.
	 */
	public static String encodeItemStack(ItemStack itemStack) {
		var nbt = new NbtCompound();
		itemStack.writeNbt(nbt);

		return nbt.asString();
	}

	/**
	 * Decodes an `ItemStack` from a JSON string as received from database storage.
	 */
	public static ItemStack decodeItemStack(String encodedStack) {
		try {
			NbtElement element = StringNbtReader.parse(encodedStack);

			if (!(element instanceof NbtCompound compound)) {
				throw new IllegalArgumentException(
						"Can not decode NBT compound from given representation, element is not an NBT compound.");
			}

			return ItemStack.fromNbt(compound);
		} catch (CommandSyntaxException exception) {
			throw new IllegalArgumentException("Could not decode item stack from given representation.", exception);
		}
	}

	// Item Stack Description

	public static String getItemStackDescription(ItemStack itemStack) {
		return itemStack.getName().getString();
	}

}
