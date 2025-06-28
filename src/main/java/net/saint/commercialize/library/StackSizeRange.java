package net.saint.commercialize.library;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(StackSizeRangeAdapter.class)
public class StackSizeRange {

	public int min;
	public int max;

	public StackSizeRange(int min, int max) {
		this.min = min;
		this.max = max;
	}

}
