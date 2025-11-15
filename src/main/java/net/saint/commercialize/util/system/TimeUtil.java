package net.saint.commercialize.util.system;

public final class TimeUtil {

	public static long getCurrentTime() {
		return System.currentTimeMillis() / 1_000L;
	}

}
