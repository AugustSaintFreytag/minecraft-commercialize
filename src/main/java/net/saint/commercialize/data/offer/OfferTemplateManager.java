package net.saint.commercialize.data.offer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.saint.commercialize.data.common.Availability;

public final class OfferTemplateManager {

	// Properties

	private List<OfferTemplate> templates = new ArrayList<>();

	private Map<Identifier, List<OfferTemplate>> templatesByItemId = new HashMap<>();

	private int[] weightSums;
	private int weightSum;

	private boolean isDirty = false;

	// Access

	public int size() {
		return templates.size();
	}

	public List<OfferTemplate> getTemplatesForItem(Identifier identifier) {
		if (isDirty) {
			rebuildIndex();
		}

		return templatesByItemId.getOrDefault(identifier, List.of());
	}

	public Optional<OfferTemplate> getRandomTemplate(Random random) {
		if (isDirty) {
			rebuildIndex();
		}

		if (weightSum <= 0) {
			return Optional.empty();
		}

		var randomWeightIndex = random.nextInt(weightSum);
		var randomIndex = Arrays.binarySearch(weightSums, randomWeightIndex + 1);

		if (randomIndex < 0) {
			randomIndex = -randomIndex - 1;
		}

		return Optional.of(templates.get(randomIndex));
	}

	// Mutation

	public void registerTemplate(OfferTemplate template) {
		templates.add(template);
		markDirty();
	}

	public void clearTemplates() {
		templates.clear();
		markDirty();
	}

	// Index

	private void markDirty() {
		isDirty = true;
	}

	private void rebuildIndex() {
		var numberOfTemplates = templates.size();

		weightSums = new int[numberOfTemplates];
		weightSum = 0;
		var indexedTemplates = new HashMap<Identifier, List<OfferTemplate>>();

		for (var i = 0; i < numberOfTemplates; i++) {
			var template = templates.get(i);
			var weight = Availability.weightForAvailability(template.availability);

			weightSum += weight;
			weightSums[i] = weightSum;

			indexedTemplates.computeIfAbsent(template.item, identifier -> new ArrayList<>()).add(template);
		}

		var finalizedIndex = new HashMap<Identifier, List<OfferTemplate>>(indexedTemplates.size());
		indexedTemplates.forEach((identifier, list) -> finalizedIndex.put(identifier, List.copyOf(list)));
		templatesByItemId = finalizedIndex;

		isDirty = false;
	}

}
