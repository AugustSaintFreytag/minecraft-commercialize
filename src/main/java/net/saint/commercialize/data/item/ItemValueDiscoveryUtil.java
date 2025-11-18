package net.saint.commercialize.data.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;

public final class ItemValueDiscoveryUtil {

	// Entry

	public static void discoverAndRegisterItemValues(MinecraftServer server) {
		var discoveredValues = discoverItemValues(server);

		for (var entry : discoveredValues.entrySet()) {
			Commercialize.ITEM_MANAGER.registerItemValue(entry.getKey(), entry.getValue());
		}
	}

	public static Map<Identifier, Integer> discoverItemValues(MinecraftServer server) {
		var registryManager = server.getRegistryManager();
		var recipeManager = server.getRecipeManager();
		var recipeEntries = recipeManager.values();

		if (recipeEntries.isEmpty()) {
			return Map.of();
		}

		var seededValues = Commercialize.ITEM_MANAGER.getValuesByItem();
		var lockedItemIds = Set.copyOf(seededValues.keySet());
		var resolvedValues = new HashMap<Identifier, Integer>(seededValues);
		var discoveredItemIds = new HashSet<Identifier>();

		var iteration = 0;
		var maxIterations = recipeEntries.size();
		var progress = true;

		while (progress && iteration < maxIterations) {
			progress = false;
			iteration++;

			for (var recipe : recipeEntries) {
				if (registerRecipeValue(recipe, registryManager, resolvedValues, lockedItemIds, discoveredItemIds)) {
					progress = true;
				}
			}
		}

		var unresolvedOutputs = countUnresolvedOutputs(recipeEntries, registryManager, resolvedValues, lockedItemIds);

		if (discoveredItemIds.isEmpty()) {
			Commercialize.LOGGER.info(
					"No additional item values discovered after {} pass(es); {} recipe output(s) unresolved.",
					iteration,
					unresolvedOutputs
			);
		}

		Commercialize.LOGGER.info(
				"Discovered {} item value(s) in {} pass(es); {} recipe output(s) unresolved.",
				discoveredItemIds.size(),
				iteration,
				unresolvedOutputs
		);

		return resolvedValues;
	}

	// Discovery

	private static boolean registerRecipeValue(Recipe<?> recipe, DynamicRegistryManager registryManager,
			Map<Identifier, Integer> resolvedValues,
			Set<Identifier> lockedItemIds, Set<Identifier> discoveredItemIds) {
		var outputStack = recipe.getOutput(registryManager);

		if (outputStack.isEmpty()) {
			return false;
		}

		var outputItemId = Registries.ITEM.getId(outputStack.getItem());

		if (lockedItemIds.contains(outputItemId)) {
			return false;
		}

		var recipeValue = resolveRecipeValue(recipe, registryManager, resolvedValues);

		if (recipeValue.isEmpty()) {
			return false;
		}

		var resolvedValue = recipeValue.get();
		var existingValue = resolvedValues.get(outputItemId);

		if (existingValue != null && existingValue <= resolvedValue) {
			return false;
		}

		var isNewItem = existingValue == null;
		resolvedValues.put(outputItemId, resolvedValue);

		if (isNewItem) {
			discoveredItemIds.add(outputItemId);
		}

		return true;
	}

	private static Optional<Integer> resolveRecipeValue(Recipe<?> recipe, DynamicRegistryManager registryManager,
			Map<Identifier, Integer> resolvedValues) {
		var recipeTypeId = Registries.RECIPE_TYPE.getId(recipe.getType());
		var ingredients = recipe.getIngredients();

		if (!isSupportedRecipeType(recipeTypeId) || ingredients.isEmpty()) {
			return Optional.empty();
		}

		var output = recipe.getOutput(registryManager);

		if (output.isEmpty()) {
			return Optional.empty();
		}

		var totalInputValue = 0;

		for (var ingredient : ingredients) {
			var ingredientValue = resolveIngredientValue(ingredient, resolvedValues);

			if (ingredientValue.isEmpty()) {
				return Optional.empty();
			}

			totalInputValue += ingredientValue.get();
		}

		var effortValue = getRecipeEffortValueForType(recipeTypeId);
		totalInputValue += effortValue;

		var resultCount = Math.max(output.getCount(), 1);
		var perItemValue = (int) Math.ceil((double) totalInputValue / resultCount);

		// Round for aesthetic purposes
		perItemValue += perItemValue % 2;

		if (perItemValue <= 0) {
			return Optional.empty();
		}

		return Optional.of(perItemValue);
	}

	private static Optional<Integer> resolveIngredientValue(Ingredient ingredient, Map<Identifier, Integer> resolvedValues) {
		if (ingredient.isEmpty()) {
			return Optional.of(0);
		}

		var matchingStacks = ingredient.getMatchingStacks();

		if (matchingStacks.length == 0) {
			return Optional.empty();
		}

		var cheapestValue = Integer.MAX_VALUE;
		var foundValue = false;

		for (var matchingStack : matchingStacks) {
			if (matchingStack.isEmpty()) {
				continue;
			}

			var matchingItemId = Registries.ITEM.getId(matchingStack.getItem());
			var baseValue = resolvedValues.get(matchingItemId);

			if (baseValue == null) {
				continue;
			}

			var stackCount = Math.max(matchingStack.getCount(), 1);
			var stackValue = (long) baseValue * stackCount;

			if (stackValue <= 0) {
				continue;
			}

			if (stackValue > Integer.MAX_VALUE) {
				stackValue = Integer.MAX_VALUE;
			}

			if (stackValue < cheapestValue) {
				cheapestValue = (int) stackValue;
				foundValue = true;
			}
		}

		if (!foundValue) {
			return Optional.empty();
		}

		return Optional.of(cheapestValue);
	}

	private static boolean isSupportedRecipeType(Identifier recipeTypeId) {
		return ItemValueDiscoveryPresets.isSupportedRecipeType(recipeTypeId);
	}

	private static int getRecipeEffortValueForType(Identifier recipeTypeId) {
		return ItemValueDiscoveryPresets.getRecipeEffortValue(recipeTypeId);
	}

	private static int countUnresolvedOutputs(Collection<Recipe<?>> recipeEntries, DynamicRegistryManager registryManager,
			Map<Identifier, Integer> resolvedValues, Set<Identifier> lockedItemIds) {
		var unresolved = 0;

		for (var recipe : recipeEntries) {
			var outputStack = recipe.getOutput(registryManager);

			if (outputStack.isEmpty()) {
				continue;
			}

			var outputItemId = Registries.ITEM.getId(outputStack.getItem());

			if (lockedItemIds.contains(outputItemId)) {
				continue;
			}

			if (!resolvedValues.containsKey(outputItemId)) {
				unresolved++;
			}
		}

		return unresolved;
	}

}
