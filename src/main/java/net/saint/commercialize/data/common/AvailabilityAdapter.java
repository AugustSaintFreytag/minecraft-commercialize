package net.saint.commercialize.data.common;

import java.io.IOException;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class AvailabilityAdapter extends TypeAdapter<Availability> {
	@Override
	public void write(JsonWriter out, Availability value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		String encodedString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, value.name());
		out.value(encodedString);
	}

	@Override
	public Availability read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		String inputString = in.nextString();
		String decodedString = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, inputString);

		try {
			return Availability.valueOf(decodedString);
		} catch (IllegalArgumentException e) {
			throw new JsonParseException("Invalid availability case value: " + inputString, e);
		}
	}
}