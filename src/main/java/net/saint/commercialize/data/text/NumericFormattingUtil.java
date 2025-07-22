package net.saint.commercialize.data.text;

public final class NumericFormattingUtil {

	public static String formatNumber(int value) {
		// Format the number with commas as thousands separators
		return String.format("%,d", value);
	}

}
