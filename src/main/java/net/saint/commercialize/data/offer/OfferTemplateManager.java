package net.saint.commercialize.data.offer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.util.math.random.Random;
import net.saint.commercialize.data.common.Availability;

public final class OfferTemplateManager {

	// Properties

	private List<OfferTemplate> templates = new ArrayList<>();

	private int[] weightSums;
	private int weightSum;

	private boolean needsIndexRebuild = false;

	// Access

	public int size() {
		return templates.size();
	}

	public Optional<OfferTemplate> getRandomTemplate(Random random) {
		if (needsIndexRebuild) {
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
		needsIndexRebuild = true;
	}

	public void clearTemplates() {
		templates.clear();
		needsIndexRebuild = true;
	}

	public void rebuildIndex() {
		var numberOfTemplates = templates.size();

		weightSums = new int[numberOfTemplates];
		weightSum = 0;

		for (int index = 0; index < numberOfTemplates; index++) {
			var template = templates.get(index);
			var weight = Availability.weightForAvailability(template.availability);

			weightSum += weight;
			weightSums[index] = weightSum;
		}

		needsIndexRebuild = false;
	}

}
