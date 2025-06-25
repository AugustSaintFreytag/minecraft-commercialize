package net.saint.commercialize.util;

public final class NumericFormattingUtil {

	public static String formatCurrency(int value) {
		var formattedValue = formatNumber(value);
		return formattedValue + " " + LocalizationUtil.localizedString("text", "currency");
	}

	public static String formatNumber(int value) {
		// Format the number with commas as thousands separators
		return String.format("%,d", value);
	}

}
