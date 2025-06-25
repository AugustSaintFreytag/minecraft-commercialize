package net.saint.commercialize.util;

import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;

public final class LocalizationUtil {

	public static Text localizedText(String category, String path) {
		var key = String.format("%s.%s.%s", category, Commercialize.MOD_ID, path);
		return Text.translatable(key);
	}

}
