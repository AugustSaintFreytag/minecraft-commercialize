package net.saint.commercialize.data.text;

import net.minecraft.text.Text;
import net.saint.commercialize.util.LocalizationUtil;

public final class CurrencyFormattingUtil {

	private static final String CURRENCY_SYMBOL = "¤";

	/**
	 * Returns a formatted currency value in string form for debugging.
	 * 
	 * Use this function only for server-side logging and debugging outputs.
	 * For all player-facing logic, use the localized `currencyText` function instead.
	 */
	public static String currencyString(int value) {
		var formattedValue = NumericFormattingUtil.formatNumber(value);
		return formattedValue + " " + CURRENCY_SYMBOL;
	}

	/**
	 * Returns a formatted currency value as a text template to be localized client-side.
	 * 
	 * Use this function for all player-facing text formulation. The currency symbol used
	 * will depend on the player's selected locale. The returned `Text` can *not* be resolved 
	 * server-side as localization is not available.
	 */
	public static Text currencyText(int value) {
		var formattedValue = NumericFormattingUtil.formatNumber(value);
		var text = Text.empty().append(Text.of(formattedValue)).append(Text.of(" "))
				.append(LocalizationUtil.localizedText("text", "currency"));

		return text;
	}

}
