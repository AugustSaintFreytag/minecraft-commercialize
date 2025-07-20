package net.saint.commercialize.data.text;

import net.saint.commercialize.util.LocalizationUtil;

public final class CurrencyFormattingUtil {

	public static String formatCurrency(int value) {
		var formattedValue = NumericFormattingUtil.formatNumber(value);
		return formattedValue + " " + LocalizationUtil.localizedString("text", "currency");
	}

}
