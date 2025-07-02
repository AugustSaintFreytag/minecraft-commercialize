package net.saint.commercialize.util;

public final class TextFormattingUtil {

	public static String capitalize(String s) {
		if (s.isEmpty()) {
			return s;
		}

		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

}
