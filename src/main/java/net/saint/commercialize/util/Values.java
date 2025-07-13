package net.saint.commercialize.util;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.nbt.NbtCompound;

public final class Values {

	// General

	public static <T> void ifPresent(T value, Consumer<? super T> action) {
		if (value != null) {
			action.accept(value);
		}
	}

	public static <T, U> U returnIfPresent(T value, Function<? super T, ? extends U> action, U fallback) {
		if (value != null) {
			return action.apply(value);
		}

		return fallback;
	}

	// NBT

	public static <U> U ifPresentAsString(NbtCompound nbt, String key, Function<String, ? extends U> action, U fallback) {
		if (nbt.contains(key)) {
			return action.apply(nbt.getString(key));
		}

		return fallback;
	}

}
