package net.saint.commercialize.data.common;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.Identifier;

public class IdentifierAdapter extends TypeAdapter<Identifier> {
	@Override
	public void write(JsonWriter w, Identifier id) throws IOException {
		if (id == null) {
			w.nullValue();
		} else {
			w.value(id.toString());
		}
	}

	@Override
	public Identifier read(JsonReader r) throws IOException {
		String s = r.nextString();
		if (s == null) {
			return null;
		}

		return Identifier.tryParse(s);
	}
}
