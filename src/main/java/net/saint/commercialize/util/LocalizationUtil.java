package net.saint.commercialize.util;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;

public final class LocalizationUtil {

	public static Text localizedText(String category, String path, Object... args) {
		var key = String.format("%s.%s.%s", category, Commercialize.MOD_ID, path);
		return Text.translatable(key, args);
	}

	public static String localizedString(String category, String path, Object... args) {
		var key = String.format("%s.%s.%s", category, Commercialize.MOD_ID, path);
		return I18n.translate(key, args);
	}

}
