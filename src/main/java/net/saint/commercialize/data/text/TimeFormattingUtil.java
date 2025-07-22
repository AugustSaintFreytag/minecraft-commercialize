package net.saint.commercialize.data.text;

import net.saint.commercialize.util.LocalizationUtil;

public final class TimeFormattingUtil {

	private static final int NUMBER_OF_TICKS_PER_DAY = 24_000;

	public static String formattedTime(long tickCount) {
		// Convert ticks to total hours
		var totalNumberOfHours = tickCount / (double) (NUMBER_OF_TICKS_PER_DAY / 24);

		// Fractional Hours

		if (totalNumberOfHours < 0.15) {
			return LocalizationUtil.localizedString("text", "time.short.moment");
		}

		if (totalNumberOfHours < 0.4) {
			var formattedOutput = new StringBuilder();
			formattedOutput.append("¼").append(" ").append(LocalizationUtil.localizedString("text", "time.short.hour"));

			return formattedOutput.toString();
		}

		if (totalNumberOfHours < 0.7) {
			var formattedOutput = new StringBuilder();
			formattedOutput.append("½").append(" ").append(LocalizationUtil.localizedString("text", "time.short.hour"));

			return formattedOutput.toString();
		}

		var totalNumberOfDays = (int) (totalNumberOfHours / 24);
		var remainingNumberOfHours = Math.max(1, (int) totalNumberOfHours % 24);

		// Hours

		if (totalNumberOfDays <= 1) {
			var formattedOutput = new StringBuilder();

			if (formattedOutput.length() > 0) {
				formattedOutput.append(", ");
			}

			var hourKey = remainingNumberOfHours == 1 ? "time.short.hour" : "time.short.hours";
			formattedOutput.append(remainingNumberOfHours).append(" ").append(LocalizationUtil.localizedString("text", hourKey));

			return formattedOutput.toString();
		}

		// Days

		var formattedOutput = new StringBuilder();
		var dayLocalizationKey = totalNumberOfDays == 1 ? "time.short.day" : "time.short.days";

		formattedOutput.append(totalNumberOfDays).append(" ").append(LocalizationUtil.localizedString("text", dayLocalizationKey));

		return formattedOutput.toString();
	}
}
