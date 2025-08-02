package net.saint.commercialize.item;

import java.util.List;

import com.mrcrayfish.furniture.refurbished.client.util.ScreenHelper;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.util.LocalizationUtil;

public class LetterItem extends Item {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "letter");

	private static final String MESSAGE_NBT_KEY = "Message";

	// Init

	public LetterItem(Settings settings) {
		super(settings);
	}

	// NBT

	public static String getMessage(ItemStack stack) {
		var nbt = stack.getOrCreateNbt();
		return nbt.getString(MESSAGE_NBT_KEY);
	}

	public static void putMessage(ItemStack stack, String message) {
		var nbt = stack.getOrCreateNbt();
		nbt.putString(MESSAGE_NBT_KEY, message);
	}

	// Name

	@Override
	public Text getName(ItemStack stack) {
		var message = getMessage(stack);

		if (message.isEmpty()) {
			return LocalizationUtil.localizedText("item", "letter_blank");
		}

		return LocalizationUtil.localizedText("item", "letter");
	}

	// Tooltip

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		var message = getMessage(stack);

		if (message.isEmpty()) {
			tooltip.add(LocalizationUtil.localizedText("text", "letter.tooltip.blank").copy().formatted(Formatting.GRAY));
			return;
		}

		ScreenHelper.splitText(message, 170).forEach(component -> {
			tooltip.add(component.formatted(Formatting.GRAY));
		});
	}

}
