package net.saint.commercialize.data.text;

import net.saint.commercialize.util.LocalizationUtil;

public final class TimeFormattingUtil {

	private static final int NUMBER_OF_TICKS_PER_DAY = 24_000;

	public static String formattedTime(long tickCount) {
		// Convert ticks to total hours
		var totalNumberOfHours = tickCount / (NUMBER_OF_TICKS_PER_DAY / 24);

		// Less than one hour
		if (totalNumberOfHours < 1) {
			return LocalizationUtil.localizedString("text", "time.short.moment");
		}

		var totalNumberOfDays = totalNumberOfHours / 24;
		var remainingNumberOfHours = totalNumberOfHours % 24;
		var formattedOutput = new StringBuilder();

		// Days portion
		if (totalNumberOfDays > 0) {
			var dayLocalizationKey = totalNumberOfDays == 1 ? "time.short.day" : "time.short.days";
			formattedOutput.append(totalNumberOfDays).append(" ").append(LocalizationUtil.localizedString("text", dayLocalizationKey));
		}

		// Hours portion
		if (remainingNumberOfHours > 0) {
			if (formattedOutput.length() > 0) {
				formattedOutput.append(", ");
			}

			var hourKey = remainingNumberOfHours == 1 ? "time.short.hour" : "time.short.hours";
			formattedOutput.append(remainingNumberOfHours).append(" ").append(LocalizationUtil.localizedString("text", hourKey));
		}

		return formattedOutput.toString();
	}
}
