package net.saint.commercialize.library;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(AvailabilityAdapter.class)
public enum Availability {

	// Cases

	EXTREMELY_RARE, VERY_RARE, UNCOMMON, COMMON, VERY_COMMON, EXTREMELY_COMMON;

	// Properties

	private static final Map<Availability, Integer> weightByAvailability = new HashMap<>() {
		{
			this.put(EXTREMELY_COMMON, 20);
			this.put(VERY_COMMON, 15);
			this.put(COMMON, 10);
			this.put(UNCOMMON, 5);
			this.put(VERY_RARE, 2);
			this.put(EXTREMELY_RARE, 1);
		}
	};

	// Mapping

	public static int weightForAvailability(Availability availability) {
		return weightByAvailability.getOrDefault(availability, 0);
	}
}
