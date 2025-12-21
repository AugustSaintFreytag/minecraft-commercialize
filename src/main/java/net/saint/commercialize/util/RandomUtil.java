package net.saint.commercialize.util;

import net.minecraft.util.math.random.Random;

public final class RandomUtil {

	public static <T> T getFromArray(Random random, T[] list) {
		if (list.length == 0) {
			return null;
		}

		var index = random.nextInt(list.length);
		return list[index];
	}

}
