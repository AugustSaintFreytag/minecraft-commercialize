package net.saint.commercialize.data.payment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.util.Identifier;

public final class Currency {

	public static final Map<Identifier, Integer> CURRENCY_VALUES = new HashMap<>() {
		{
			this.put(new Identifier("numismatics", "spur"), 1);
			this.put(new Identifier("numismatics", "bevel"), 8);
			this.put(new Identifier("numismatics", "sprocket"), 16);
			this.put(new Identifier("numismatics", "cog"), 64);
			this.put(new Identifier("numismatics", "crown"), 512);
			this.put(new Identifier("numismatics", "sun"), 4096);
		}
	};

	public static final List<Entry<Identifier, Integer>> ORDERED_CURRENCY_VALUES = CURRENCY_VALUES.entrySet().stream()
			.sorted((lhs, rhs) -> {
				return lhs.getValue() < rhs.getValue() ? 1 : -1;
			}).toList();
}
