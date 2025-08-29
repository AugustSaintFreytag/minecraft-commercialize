package net.saint.commercialize.data.common;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(AvailabilityAdapter.class)
public enum Availability {

	// Cases

	EXTREMELY_RARE, VERY_RARE, RARE, UNCOMMON, COMMON, VERY_COMMON, EXTREMELY_COMMON;

	// Properties

	private static final Map<Availability, Integer> weightByAvailability = new HashMap<>() {
		{
			this.put(EXTREMELY_COMMON, 50);
			this.put(VERY_COMMON, 30);
			this.put(COMMON, 25);
			this.put(UNCOMMON, 10);
			this.put(RARE, 8);
			this.put(VERY_RARE, 4);
			this.put(EXTREMELY_RARE, 1);
		}
	};

	// Mapping

	public static int weightForAvailability(Availability availability) {
		return weightByAvailability.getOrDefault(availability, 0);
	}
}
