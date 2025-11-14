package net.saint.commercialize.util.localization;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;

public final class LocalizationUtil {

	public static String key(String category, String path) {
		return String.format("%s.%s.%s", category, Commercialize.MOD_ID, path);
	}

	public static Text localizedText(String category, String path, Object... args) {
		var key = key(category, path);
		return Text.translatable(key, args);
	}

	public static String localizedString(String category, String path, Object... args) {
		var key = key(category, path);
		return I18n.translate(key, args);
	}

}
