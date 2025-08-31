package net.saint.commercialize.util;

import java.util.List;

import net.minecraft.text.Text;

public final class TextUtil {

	public static Text joinTexts(List<Text> texts) {
		var mutableText = Text.empty().copy();

		for (var text : texts) {
			mutableText.append(text);
		}

		return mutableText;
	}

	public static Text joinTexts(List<Text> texts, Text separator) {
		if (separator == null) {
			return joinTexts(texts);
		}

		var mutableText = Text.empty().copy();

		for (int index = 0; index < texts.size(); index++) {
			if (index > 0) {
				mutableText.append(separator);
			}

			mutableText.append(texts.get(index));
		}

		return mutableText;
	}

}
