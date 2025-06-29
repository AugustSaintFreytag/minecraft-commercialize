package net.saint.commercialize.data.offer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	public List<OfferTemplate> getTemplates() {
		return templates;
	}

	public OfferTemplate getRandomTemplate(Random random) {
		if (needsIndexRebuild) {
			rebuildIndex();
		}

		if (weightSum <= 0) {
			return null;
		}

		var randomWeightIndex = random.nextInt(weightSum);
		var randomIndex = Arrays.binarySearch(weightSums, randomWeightIndex + 1);

		if (randomIndex < 0) {
			randomIndex = -randomIndex - 1;
		}

		return templates.get(randomIndex);
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
		var n = templates.size();

		weightSums = new int[n];
		weightSum = 0;

		for (int index = 0; index < n; index++) {
			var weight = Availability.weightForAvailability(templates.get(index).availability);

			weightSum += weight;
			weightSums[index] = weightSum;
		}

		needsIndexRebuild = false;
	}

}
