package net.saint.commercialize.data.text;

public final class TimePreset {

	private static final int NUMBER_OF_TICKS_PER_DAY = 24_000;

	public static final long ONE_HOUR = NUMBER_OF_TICKS_PER_DAY / 24;
	public static final long TWO_HOURS = ONE_HOUR * 2;
	public static final long SIX_HOURS = ONE_HOUR * 6;
	public static final long TWELVE_HOURS = ONE_HOUR * 12;

	public static final long ONE_DAY = NUMBER_OF_TICKS_PER_DAY;
	public static final long THREE_DAYS = NUMBER_OF_TICKS_PER_DAY * 3;
	public static final long FIVE_DAYS = NUMBER_OF_TICKS_PER_DAY * 5;
	public static final long ONE_WEEK = NUMBER_OF_TICKS_PER_DAY * 7;
	public static final long TWO_WEEKS = NUMBER_OF_TICKS_PER_DAY * 14;

}
