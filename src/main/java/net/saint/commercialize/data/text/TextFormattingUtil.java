package net.saint.commercialize.data.text;

public final class TextFormattingUtil {

	public static String capitalizedString(String s) {
		if (s.isEmpty()) {
			return s;
		}

		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

}
