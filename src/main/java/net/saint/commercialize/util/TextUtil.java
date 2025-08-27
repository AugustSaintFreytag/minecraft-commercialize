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

}
