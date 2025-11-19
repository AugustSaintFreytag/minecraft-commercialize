package net.saint.commercialize.data.valuation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;

final class ItemValueDiscoveryStatsWriter {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ItemValueDiscoveryStatsWriter() {
	}

	static void write(Map<Identifier, Integer> resolvedValues, Set<Identifier> lockedIds, Set<Identifier> encounteredResultIds,
			Map<Identifier, Set<Identifier>> unsupportedRecipeTypes) {
		var discoveredIds = new HashSet<>(resolvedValues.keySet());
		discoveredIds.removeAll(lockedIds);

		var unresolvedIds = new HashSet<Identifier>();

		for (var identifier : encounteredResultIds) {
			if (!resolvedValues.containsKey(identifier)) {
				unresolvedIds.add(identifier);
			}
		}

		var stats = new JsonObject();

		stats.add("configuredIds", toJsonArray(lockedIds));
		stats.add("discoveredIds", toJsonArray(discoveredIds));
		stats.add("unresolvedIds", toJsonArray(unresolvedIds));
		stats.add("unsupportedTypes", toJsonObject(unsupportedRecipeTypes));

		try {
			var statsDir = Commercialize.MOD_CONFIG_DIR.resolve("stats");
			Files.createDirectories(statsDir);

			var statsFile = statsDir.resolve("discovery-values.json");
			Files.writeString(statsFile, GSON.toJson(stats), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			Commercialize.LOGGER.error("Could not write value discovery stats.", e);
		}
	}

	private static JsonArray toJsonArray(Collection<Identifier> identifiers) {
		var array = new JsonArray();
		identifiers.stream()
				.map(Identifier::toString)
				.sorted()
				.forEach(array::add);
		return array;
	}

	private static JsonObject toJsonObject(Map<Identifier, Set<Identifier>> unsupportedRecipeTypes) {
		var obj = new JsonObject();
		unsupportedRecipeTypes.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
				.forEach(entry -> obj.add(entry.getKey().toString(), toJsonArray(entry.getValue())));
		return obj;
	}

}
