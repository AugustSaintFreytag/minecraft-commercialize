package net.saint.commercialize.data.text;

import net.saint.commercialize.Commercialize;

public final class TimePreset {

	private static long oneHourTicks;
	private static long twoHoursTicks;
	private static long sixHoursTicks;
	private static long twelveHoursTicks;
	private static long oneDayTicks;
	private static long threeDaysTicks;
	private static long fiveDaysTicks;
	private static long oneWeekTicks;
	private static long twoWeeksTicks;

	static {
		reload();
	}

	private TimePreset() {
	}

	public static void reload() {
		var ticksPerDay = Commercialize.CONFIG.ticksPerDay;

		oneHourTicks = ticksPerDay / 24;
		twoHoursTicks = oneHourTicks * 2;
		sixHoursTicks = oneHourTicks * 6;
		twelveHoursTicks = oneHourTicks * 12;
		oneDayTicks = ticksPerDay;
		threeDaysTicks = ticksPerDay * 3;
		fiveDaysTicks = ticksPerDay * 5;
		oneWeekTicks = ticksPerDay * 7;
		twoWeeksTicks = ticksPerDay * 14;
	}

	public static long oneHour() {
		return oneHourTicks;
	}

	public static long twoHours() {
		return twoHoursTicks;
	}

	public static long sixHours() {
		return sixHoursTicks;
	}

	public static long twelveHours() {
		return twelveHoursTicks;
	}

	public static long oneDay() {
		return oneDayTicks;
	}

	public static long threeDays() {
		return threeDaysTicks;
	}

	public static long fiveDays() {
		return fiveDaysTicks;
	}

	public static long oneWeek() {
		return oneWeekTicks;
	}

	public static long twoWeeks() {
		return twoWeeksTicks;
	}

}
