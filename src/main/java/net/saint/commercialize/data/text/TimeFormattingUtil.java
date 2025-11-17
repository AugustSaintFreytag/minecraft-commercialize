package net.saint.commercialize.data.text;

import net.minecraft.text.Text;
import net.saint.commercialize.util.LocalizationUtil;

public final class TimeFormattingUtil {

	public static Text formattedTime(long tickCount) {
		var totalNumberOfHours = tickCount / (double) TimePreset.oneHour();

		// Fractional Hours
		if (totalNumberOfHours < 0.15) {
			return LocalizationUtil.localizedText("text", "time.short.moment");
		}

		if (totalNumberOfHours < 0.4) {
			return Text.literal("¼ ").append(LocalizationUtil.localizedText("text", "time.short.hour"));
		}

		if (totalNumberOfHours < 0.7) {
			return Text.literal("½ ").append(LocalizationUtil.localizedText("text", "time.short.hour"));
		}

		var totalNumberOfDays = (int) (tickCount / (double) TimePreset.oneDay());
		var remainingNumberOfHours = Math.max(1, (int) totalNumberOfHours % 24);

		// Hours
		if (totalNumberOfDays < 1) {
			var hourKey = remainingNumberOfHours == 1 ? "time.short.hour" : "time.short.hours";
			return Text.literal(String.valueOf(remainingNumberOfHours)).append(Text.literal(" "))
					.append(LocalizationUtil.localizedText("text", hourKey));
		}

		// Days
		var dayLocalizationKey = totalNumberOfDays == 1 ? "time.short.day" : "time.short.days";
		return Text.literal(String.valueOf(totalNumberOfDays)).append(Text.literal(" "))
				.append(LocalizationUtil.localizedText("text", dayLocalizationKey));
	}
}
