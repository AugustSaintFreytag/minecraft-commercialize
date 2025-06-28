package net.saint.commercialize.library;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(AvailabilityAdapter.class)
public enum Availability {
	EXTREMELY_RARE, VERY_RARE, UNCOMMON, COMMON, VERY_COMMON, EXTREMELY_COMMON
}
