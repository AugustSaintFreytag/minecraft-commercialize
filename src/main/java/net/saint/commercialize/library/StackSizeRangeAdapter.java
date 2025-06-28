package net.saint.commercialize.library;

import java.io.IOException;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter for encoding and decoding a stack size range in string form.
 */
public class StackSizeRangeAdapter extends TypeAdapter<StackSizeRange> {

	private static final String RANGE_SEPARATOR = "-";

	@Override
	public void write(JsonWriter out, StackSizeRange value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}

		if (value.min == value.max) {
			out.value(Integer.toString(value.min));
		} else {
			out.value(value.min + RANGE_SEPARATOR + value.max);
		}
	}

	@Override
	public StackSizeRange read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}

		var token = in.nextString();

		try {
			if (!token.contains(RANGE_SEPARATOR)) {
				var v = Integer.parseInt(token.trim());
				return new StackSizeRange(v, v);
			}

			var parts = token.split(RANGE_SEPARATOR, 2);
			var min = Integer.parseInt(parts[0].trim());
			var max = Integer.parseInt(parts[1].trim());

			return new StackSizeRange(min, max);
		} catch (NumberFormatException e) {
			throw new JsonParseException("Can not decode stack size with invalid range: " + token, e);
		}
	}
}
