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
import net.saint.commercialize.util.LocalizationUtil;

public class LetterItem extends Item {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "letter");

	private static final String MESSAGE_NBT_KEY = "Message";
	private static final String SUBJECT_NBT_KEY = "Subject";
	private static final String AUTHOR_NBT_KEY = "Author";
	private static final String MESSAGE_TEXT_NBT_KEY = "MessageText";
	private static final String SUBJECT_TEXT_NBT_KEY = "SubjectText";
	private static final String AUTHOR_TEXT_NBT_KEY = "AuthorText";

	// Init

	public LetterItem(Settings settings) {
		super(settings);
	}

	// NBT

	public static String getMessage(ItemStack stack) {
		var nbt = stack.getOrCreateNbt();
		return nbt.getString(MESSAGE_NBT_KEY);
	}

	public static Text getMessageText(ItemStack stack) {
		return getSerializedText(stack, MESSAGE_TEXT_NBT_KEY);
	}

	public static void putMessage(ItemStack stack, String message) {
		putMessage(stack, Text.literal(message));
	}

	public static void putMessage(ItemStack stack, Text message) {
		putText(stack, MESSAGE_NBT_KEY, MESSAGE_TEXT_NBT_KEY, message);
	}

	public static String getSubject(ItemStack stack) {
		var nbt = stack.getOrCreateNbt();
		return nbt.getString(SUBJECT_NBT_KEY);
	}

	public static Text getSubjectText(ItemStack stack) {
		return getSerializedText(stack, SUBJECT_TEXT_NBT_KEY);
	}

	public static void putSubject(ItemStack stack, String subject) {
		putSubject(stack, Text.literal(subject));
	}

	public static void putSubject(ItemStack stack, Text subject) {
		putText(stack, SUBJECT_NBT_KEY, SUBJECT_TEXT_NBT_KEY, subject);
	}

	public static String getAuthor(ItemStack stack) {
		var nbt = stack.getOrCreateNbt();
		return nbt.getString(AUTHOR_NBT_KEY);
	}

	public static Text getAuthorText(ItemStack stack) {
		return getSerializedText(stack, AUTHOR_TEXT_NBT_KEY);
	}

	public static void putAuthor(ItemStack stack, String author) {
		putAuthor(stack, Text.literal(author));
	}

	public static void putAuthor(ItemStack stack, Text author) {
		putText(stack, AUTHOR_NBT_KEY, AUTHOR_TEXT_NBT_KEY, author);
	}

	// Name

	@Override
	public Text getName(ItemStack stack) {
		var subjectText = getSubjectText(stack);

		if (subjectText != null) {
			// #8079ffff
			return subjectText.copy().setStyle(Style.EMPTY.withColor(0x8079ff));
		}

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
		var messageText = getMessageText(stack);
		var message = getMessage(stack);
		var authorText = getAuthorText(stack);
		var author = getAuthor(stack);

		if (authorText != null) {
			tooltip.add(authorText.copy().formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
		} else if (!author.isEmpty()) {
			tooltip.add(Text.of(author).copy().formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
		}

		if (messageText != null) {
			var resolved = messageText.getString();

			if (resolved.isEmpty()) {
				return;
			}

			ScreenHelper.splitText(resolved, 170).forEach(component -> {
				tooltip.add(component.formatted(Formatting.WHITE));
			});
			return;
		}

		if (message.isEmpty()) {
			return;
		}

		ScreenHelper.splitText(message, 170).forEach(component -> {
			tooltip.add(component.formatted(Formatting.WHITE));
		});
	}

	// Serialization

	private static Text getSerializedText(ItemStack stack, String key) {
		if (!stack.hasNbt()) {
			return null;
		}

		var nbt = stack.getNbt();

		if (nbt == null || !nbt.contains(key)) {
			return null;
		}

		var encoded = nbt.getString(key);
		return Text.Serializer.fromJson(encoded);
	}

	private static void putText(ItemStack stack, String legacyKey, String textKey, Text text) {
		var nbt = stack.getOrCreateNbt();
		var resolved = text == null ? Text.empty() : text;
		nbt.putString(legacyKey, resolved.getString());
		nbt.putString(textKey, Text.Serializer.toJson(resolved));
	}

}
