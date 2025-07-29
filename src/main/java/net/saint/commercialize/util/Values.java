package net.saint.commercialize.util;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.nbt.NbtCompound;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.library.CircularList;

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

	// Sequences

	public static <T> T nextValueInSequence(T[] values, T previousValue) {
		var index = ArrayUtils.indexOf(values, previousValue);
		var size = values.length;

		if (index == ArrayUtils.INDEX_NOT_FOUND) {
			Commercialize.LOGGER.error("Can not advance value in sequence from '{}', current element not found.", previousValue);
			return values[0];
		}

		return values[(index + 1) % size];
	}

	public static <T> T assertedValueInSequence(T[] values, T currentValue) {
		// Check if current value is in the sequence, if so, return.
		// If value is not in sequence, return the first value in the sequence.

		if (ArrayUtils.contains(values, currentValue)) {
			return currentValue;
		}

		return values[0];
	}

	// NBT

	public static void ifPresentAsString(NbtCompound nbt, String key, Consumer<String> action) {
		if (nbt.contains(key)) {
			action.accept(nbt.getString(key));
		}
	}

	public static <U> U returnIfPresentAsString(NbtCompound nbt, String key, Function<String, ? extends U> action, U fallback) {
		if (nbt.contains(key)) {
			return action.apply(nbt.getString(key));
		}

		return fallback;
	}

	public static <T extends Enum<T>> void readAndSelectEnumValueFromNbt(NbtCompound nbt, String key, CircularList<T> property,
			Function<String, T> parser) {
		ifPresentAsString(nbt, "sortMode", storedValue -> {
			try {
				var value = parser.apply(storedValue);
				property.select(value);
			} catch (IllegalArgumentException e) {
				Commercialize.LOGGER.warn("Could not decode value '{}' for key '{}' from NBT for property.", storedValue, key);
				return;
			}
		});
	}

}
