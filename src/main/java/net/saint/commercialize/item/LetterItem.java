package net.saint.commercialize.item;

import java.util.List;

import com.mrcrayfish.furniture.refurbished.client.util.ScreenHelper;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.text.TextFormattingUtil;
import net.saint.commercialize.util.localization.LocalizationUtil;

public class LetterItem extends Item {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "letter");

	private static final String MESSAGE_NBT_KEY = "Message";
	private static final String SUBJECT_NBT_KEY = "Subject";
	private static final String AUTHOR_NBT_KEY = "Author";

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

	public static String getSubject(ItemStack stack) {
		var nbt = stack.getOrCreateNbt();
		return nbt.getString(SUBJECT_NBT_KEY);
	}

	public static void putSubject(ItemStack stack, String subject) {
		var nbt = stack.getOrCreateNbt();
		nbt.putString(SUBJECT_NBT_KEY, subject);
	}

	public static String getAuthor(ItemStack stack) {
		var nbt = stack.getOrCreateNbt();
		return nbt.getString(AUTHOR_NBT_KEY);
	}

	public static void putAuthor(ItemStack stack, String author) {
		var nbt = stack.getOrCreateNbt();
		nbt.putString(AUTHOR_NBT_KEY, author);
	}

	// Name

	@Override
	public Text getName(ItemStack stack) {
		var subject = getSubject(stack);

		if (!subject.isEmpty()) {
			// #8079ffff
			return Text.of(TextFormattingUtil.capitalizedString(subject)).copy().setStyle(Style.EMPTY.withColor(0x8079ff));
		}

		var message = getMessage(stack);

		if (message.isEmpty()) {
			// White text
			return LocalizationUtil.localizedText("item", "letter_blank");
		}

		// #a1abf9ff
		return LocalizationUtil.localizedText("item", "letter").copy().setStyle(Style.EMPTY.withColor(0xa1abf9));
	}

	// Tooltip

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		var message = getMessage(stack);
		var author = getAuthor(stack);

		if (!author.isEmpty()) {
			tooltip.add(Text.of(author).copy().formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
		}

		if (message.isEmpty()) {
			return;
		}

		ScreenHelper.splitText(message, 170).forEach(component -> {
			tooltip.add(component.formatted(Formatting.WHITE));
		});
	}

}
