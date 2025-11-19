package net.saint.commercialize.data.valuation;

import java.util.Map;

import net.minecraft.util.Identifier;

public final class ItemValueDiscoveryPresets {

	public static final Map<Identifier, Integer> RECIPE_EFFORT_VALUE_BY_TYPE = Map.ofEntries(
			Map.entry(new Identifier("minecraft", "crafting"), 0),
			Map.entry(new Identifier("minecraft", "stonecutting"), 0),
			Map.entry(new Identifier("minecraft", "smithing"), 0),
			Map.entry(new Identifier("minecraft", "smithing_transform"), 0),
			Map.entry(new Identifier("minecraft", "smithing_trim"), 0),
			Map.entry(new Identifier("minecraft", "campfire_cooking"), 4),
			Map.entry(new Identifier("minecraft", "smelting"), 4),
			Map.entry(new Identifier("minecraft", "smoking"), 4),
			Map.entry(new Identifier("minecraft", "blasting"), 6),
			Map.entry(new Identifier("minecraft", "brewing"), 18),
			Map.entry(new Identifier("create", "compacting"), 10),
			Map.entry(new Identifier("create", "crushing"), 12),
			Map.entry(new Identifier("create", "cutting"), 10),
			Map.entry(new Identifier("create", "deploying"), 2),
			Map.entry(new Identifier("create", "item_application"), 2),
			Map.entry(new Identifier("create", "haunting"), 20),
			Map.entry(new Identifier("create", "mechanical_crafting"), 20),
			Map.entry(new Identifier("create", "milling"), 8),
			Map.entry(new Identifier("create", "mixing"), 8),
			Map.entry(new Identifier("create", "pressing"), 8),
			Map.entry(new Identifier("create", "sawing"), 4),
			Map.entry(new Identifier("create", "sandpaper_polishing"), 12),
			Map.entry(new Identifier("create", "sequenced_assembly"), 28),
			Map.entry(new Identifier("create", "splashing"), 5),
			Map.entry(new Identifier("create", "filling"), 0),
			Map.entry(new Identifier("createaddition", "rolling"), 8),
			Map.entry(new Identifier("createaddition", "charging"), 12),
			Map.entry(new Identifier("createdieselgenerators", "basin_fermenting"), 22),
			Map.entry(new Identifier("create_bic_bit", "deep_frying"), 14),
			Map.entry(new Identifier("farmersdelight", "cooking"), 10),
			Map.entry(new Identifier("farmersdelight", "cutting"), 4),
			Map.entry(new Identifier("farmersrespite", "brewing"), 10)
	);

	public static boolean isSupportedRecipeType(Identifier recipeTypeId) {
		return RECIPE_EFFORT_VALUE_BY_TYPE.containsKey(recipeTypeId);
	}

	public static int getRecipeEffortValue(Identifier recipeTypeId) {
		var effortValue = RECIPE_EFFORT_VALUE_BY_TYPE.get(recipeTypeId);

		if (effortValue == null) {
			return 0;
		}

		return effortValue;
	}

}
