package net.saint.commercialize.data.text;

import net.minecraft.text.Text;
import net.saint.commercialize.util.LocalizationUtil;

public final class CurrencyFormattingUtil {

	public static String currencyString(int value) {
		var formattedValue = NumericFormattingUtil.formatNumber(value);
		return formattedValue + " " + LocalizationUtil.localizedString("text", "currency");
	}

	public static Text currencyText(int value) {
		return Text.of(currencyString(value));
	}

}
