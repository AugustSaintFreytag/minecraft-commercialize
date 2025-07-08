package net.saint.commercialize.util;

import java.util.function.Function;

import net.minecraft.nbt.NbtCompound;

public final class Values {

	public static <T, U> U ifPresent(T value, Function<? super T, ? extends U> action, U fallback) {
		if (value != null) {
			return action.apply(value);
		}

		return fallback;
	}

	public static <U> U ifPresentAsString(NbtCompound nbt, String key, Function<String, ? extends U> action, U fallback) {
		if (nbt.contains(key)) {
			return action.apply(nbt.getString(key));
		}

		return fallback;
	}

}
