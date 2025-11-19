package net.saint.commercialize.data.valuation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.simibubi.create.foundation.fluid.FluidIngredient;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.mixin.BucketItemAccessor;

public final class ItemValueDiscoveryUtil {

	// Entry

	/**
	 * Resolves all craftable item values and stores them in the shared manager.
	 *
	 * Runs discovery to compute prices, then writes the results back so other systems can immediately reference the resolved values.
	 */
	public static void discoverAndRegisterItemValues(MinecraftServer server) {
		var discoveredValues = discoverItemValues(server);

		for (var entry : discoveredValues.entrySet()) {
			Commercialize.ITEM_MANAGER.registerItemValue(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Iteratively discovers item values by walking every recipe until convergence.
	 *
	 * Seeds known prices, repeatedly processes recipes for new data, and returns the final resolved map for further use.
	 */
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
		var resolvedFluidValues = new HashMap<Identifier, Integer>();
		var discoveredItemIds = new HashSet<Identifier>();

		seedFluidValuesFromBuckets(resolvedValues, resolvedFluidValues);

		var iteration = 0;
		var maxIterations = recipeEntries.size();
		var didProgress = true;

		while (didProgress && iteration < maxIterations) {
			didProgress = false;
			iteration++;

			for (var recipe : recipeEntries) {
				if (registerRecipeValue(recipe, registryManager, resolvedValues, resolvedFluidValues, lockedItemIds, discoveredItemIds)) {
					didProgress = true;
				}
			}
		}

		var unresolvedOutputs = countUnresolvedOutputs(recipeEntries, registryManager, resolvedValues, lockedItemIds);

		Commercialize.LOGGER.info(
				"Discovered {} item value(s) in {} pass(es); {} recipe output(s) unresolved.",
				discoveredItemIds.size(),
				iteration,
				unresolvedOutputs
		);

		return resolvedValues;
	}

	// Discovery

	/**
	 * Normalizes a recipe and attempts to register any newly resolvable outputs.
	 *
	 * Acts as the entry point for per-recipe processing, ensuring both vanilla and Create recipes go through the same downstream logic.
	 */
	private static boolean registerRecipeValue(Recipe<?> recipe, DynamicRegistryManager registryManager,
			Map<Identifier, Integer> resolvedValues, Map<Identifier, Integer> resolvedFluidValues,
			Set<Identifier> lockedItemIds, Set<Identifier> discoveredItemIds) {
		var normalizedRecipe = RecipeNormalizationUtil.normalizeRecipe(recipe, registryManager);

		if (normalizedRecipe.isEmpty() || isSupportedRecipeType(null)) {
			return false;
		}

		return registerNormalizedItemRecipe(normalizedRecipe.get(), resolvedValues, resolvedFluidValues, lockedItemIds, discoveredItemIds);
	}

	/**
	 * Stores a computed item value when it is new or cheaper than previous estimates.
	 *
	 * Protects locked seed values, keeps track of newly discovered items for logging, and prevents regressions to more expensive results.
	 */
	private static boolean applyResolvedItemValue(Identifier outputItemId, int resolvedValue, Map<Identifier, Integer> resolvedValues,
			Set<Identifier> lockedItemIds, Set<Identifier> discoveredItemIds) {
		if (lockedItemIds.contains(outputItemId)) {
			return false;
		}

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

	/**
	 * Calculates the value of a normalized recipe’s outputs and updates caches.
	 *
	 * Totals ingredient costs, applies effort bonuses, and either registers the resulting item stack or propagates value to fluid outputs.
	 */
	private static boolean registerNormalizedItemRecipe(NormalizedItemRecipe recipe, Map<Identifier, Integer> resolvedValues,
			Map<Identifier, Integer> resolvedFluidValues, Set<Identifier> lockedItemIds, Set<Identifier> discoveredItemIds) {
		var totalInputValue = resolveTotalInputValue(recipe, resolvedValues, resolvedFluidValues);

		if (totalInputValue.isEmpty()) {
			return false;
		}

		var didProgress = false;
		var outputStack = recipe.itemOutput();

		if (!outputStack.isEmpty()) {
			var perItemValue = resolvePerItemOutputValue(outputStack, totalInputValue.get());

			if (perItemValue.isPresent()) {
				var outputItemId = Registries.ITEM.getId(outputStack.getItem());
				didProgress |= applyResolvedItemValue(outputItemId, perItemValue.get(), resolvedValues, lockedItemIds, discoveredItemIds);
			}
		} else {
			didProgress |= registerFluidOutputs(
					recipe.fluidOutputs(),
					totalInputValue.get(),
					resolvedValues,
					resolvedFluidValues,
					lockedItemIds,
					discoveredItemIds
			);
		}

		return didProgress;
	}

	/**
	 * Aggregates every prerequisite cost for a normalized recipe into a single number.
	 *
	 * Adds up resolved item and fluid ingredients, applies the recipe-type effort bonus, and aborts if any dependency lacks a value.
	 */
	private static Optional<Integer> resolveTotalInputValue(NormalizedItemRecipe recipe, Map<Identifier, Integer> resolvedValues,
			Map<Identifier, Integer> resolvedFluidValues) {
		if (recipe.itemIngredients().isEmpty() && recipe.fluidIngredients().isEmpty()) {
			return Optional.empty();
		}

		var totalInputValue = 0;

		for (var ingredient : recipe.itemIngredients()) {
			var ingredientValue = resolveIngredientValue(ingredient, resolvedValues);

			if (ingredientValue.isEmpty()) {
				return Optional.empty();
			}

			totalInputValue += ingredientValue.get();
		}

		for (var fluidIngredient : recipe.fluidIngredients()) {
			var fluidValue = resolveFluidIngredientValue(fluidIngredient, resolvedValues, resolvedFluidValues);

			if (fluidValue.isEmpty()) {
				return Optional.empty();
			}

			totalInputValue += fluidValue.get();
		}

		totalInputValue += getRecipeEffortValueForType(recipe.recipeType());

		if (totalInputValue <= 0) {
			return Optional.empty();
		}

		return Optional.of(totalInputValue);
	}

	/**
	 * Returns the cheapest known price for any stack matching the given ingredient.
	 *
	 * Evaluates each possible stack expansion, multiplies base value by stack size, and keeps the smallest positive result so crafting picks the best option.
	 */
	private static Optional<Integer> resolveIngredientValue(Ingredient ingredient, Map<Identifier, Integer> resolvedValues) {
		if (ingredient.isEmpty()) {
			return Optional.of(0);
		}

		var potentialIngredientStacks = ingredient.getMatchingStacks();

		if (potentialIngredientStacks.length == 0) {
			return Optional.empty();
		}

		var cheapestValue = Integer.MAX_VALUE;
		var foundValue = false;

		for (var potentialIngredientStack : potentialIngredientStacks) {
			if (potentialIngredientStack.isEmpty()) {
				continue;
			}

			var matchingItemId = Registries.ITEM.getId(potentialIngredientStack.getItem());
			var baseValue = resolvedValues.get(matchingItemId);

			if (baseValue == null) {
				continue;
			}

			var stackCount = Math.max(potentialIngredientStack.getCount(), 1);
			var stackValue = baseValue * stackCount;

			if (stackValue == 0) {
				continue;
			}

			if (stackValue < cheapestValue) {
				cheapestValue = stackValue;
				foundValue = true;
			}
		}

		if (!foundValue) {
			return Optional.empty();
		}

		return Optional.of(cheapestValue);
	}

	/**
	 * Converts a recipe’s total input value into a per-item result price.
	 *
	 * Divides by output count, enforces rounding for nicer display, and rejects zeroed outcomes to avoid meaningless registrations.
	 */
	private static Optional<Integer> resolvePerItemOutputValue(ItemStack outputStack, int totalInputValue) {
		if (outputStack.isEmpty() || totalInputValue == 0) {
			return Optional.empty();
		}

		var resultCount = Math.max(outputStack.getCount(), 1);
		var perItemValue = totalInputValue / resultCount;

		perItemValue += perItemValue % 2;

		if (perItemValue == 0) {
			return Optional.empty();
		}

		return Optional.of(perItemValue);
	}

	/**
	 * Determines the cheapest attainable cost for a fluid ingredient.
	 *
	 * Processes all acceptable fluid stacks, calculates their value requirements, and returns the lowest to represent the optimal crafting choice.
	 */
	private static Optional<Integer> resolveFluidIngredientValue(FluidIngredient ingredient, Map<Identifier, Integer> resolvedValues,
			Map<Identifier, Integer> resolvedFluidValues) {
		var matchingStacks = ingredient.getMatchingFluidStacks();

		if (matchingStacks == null || matchingStacks.isEmpty()) {
			return Optional.empty();
		}

		var didFindValue = false;
		var cheapestValue = Integer.MAX_VALUE;

		for (var matchingStack : matchingStacks) {
			if (matchingStack == null || matchingStack.isEmpty()) {
				continue;
			}

			var unitValue = resolveFluidUnitValue(matchingStack.getFluid(), resolvedValues, resolvedFluidValues);

			if (unitValue.isEmpty()) {
				continue;
			}

			var scaledValue = scaleFluidValue(unitValue.get(), (int) matchingStack.getAmount());

			if (scaledValue < cheapestValue) {
				cheapestValue = scaledValue;
				didFindValue = true;
			}
		}

		if (!didFindValue) {
			return Optional.empty();
		}

		return Optional.of(cheapestValue);
	}

	/**
	 * Fetches the per-bucket cost for a fluid, deriving it if needed.
	 *
	 * Checks cached values first, then looks for bucket items to back 
	 * into the fluid’s price when no direct entry exists.
	 */
	private static Optional<Integer> resolveFluidUnitValue(Fluid fluid, Map<Identifier, Integer> resolvedValues,
			Map<Identifier, Integer> resolvedFluidValues) {
		if (fluid == null || fluid == Fluids.EMPTY) {
			return Optional.empty();
		}

		var fluidId = Registries.FLUID.getId(fluid);
		var resolvedValue = resolvedFluidValues.get(fluidId);

		if (resolvedValue != null) {
			return Optional.of(resolvedValue);
		}

		var bucketValue = resolveFluidValueFromBucket(fluid, resolvedValues);

		if (bucketValue.isEmpty()) {
			return Optional.empty();
		}

		resolvedFluidValues.put(fluidId, bucketValue.get());

		return bucketValue;
	}

	/**
	 * Calculates fluid value by subtracting the cost of an empty bucket from its filled variant.
	 *
	 * Enables fluid valuation even when only bucket items are preconfigured, 
	 * ensures container costs are not double-counted.
	 */
	private static Optional<Integer> resolveFluidValueFromBucket(Fluid fluid, Map<Identifier, Integer> resolvedValues) {
		Item bucketItem = fluid.getBucketItem();

		if (bucketItem == null || bucketItem == Items.AIR) {
			return Optional.empty();
		}

		var bucketItemId = Registries.ITEM.getId(bucketItem);
		var bucketItemValue = resolvedValues.get(bucketItemId);

		if (bucketItemValue == null) {
			return Optional.empty();
		}

		var emptyBucketValue = resolvedValues.getOrDefault(Registries.ITEM.getId(Items.BUCKET), 0);
		var fluidValue = Math.max(0, bucketItemValue - emptyBucketValue);

		return Optional.of(fluidValue);
	}

	/**
	 * Scales the per-bucket cost of a fluid to match a specific millibucket requirement.
	 *
	 * Uses Fabric’s bucket constant for proportional math and returns zero when inputs 
	 * are invalid to avoid polluting totals.
	 */
	private static int scaleFluidValue(int unitValue, int amount) {
		if (amount <= 0 || unitValue < 0) {
			return 0;
		}

		var scaledValue = unitValue * amount / (int) FluidConstants.BUCKET;
		return scaledValue;
	}

	/**
	 * Registers fluid output values when a recipe produces liquids instead of items.
	 *
	 * Distributes the total input cost across fluid amounts and also updates 
	 * bucket items when available to keep inventories in sync.
	 */
	private static boolean registerFluidOutputs(List<FluidStack> fluidResults, int totalInputValue,
			Map<Identifier, Integer> resolvedValues,
			Map<Identifier, Integer> resolvedFluidValues, Set<Identifier> lockedItemIds, Set<Identifier> discoveredItemIds) {
		if (fluidResults.isEmpty()) {
			return false;
		}

		var totalAmount = 0;

		for (FluidStack fluidResult : fluidResults) {
			if (fluidResult == null || fluidResult.isEmpty()) {
				continue;
			}

			totalAmount += fluidResult.getAmount();
		}

		if (totalAmount == 0) {
			return false;
		}

		var perBucketValue = totalInputValue * (int) FluidConstants.BUCKET / totalAmount;

		if (perBucketValue == 0) {
			return false;
		}

		var didUpdate = false;

		for (FluidStack fluidResult : fluidResults) {
			if (fluidResult == null || fluidResult.isEmpty()) {
				continue;
			}

			var fluid = fluidResult.getFluid();

			if (fluid == null || fluid == Fluids.EMPTY) {
				continue;
			}

			var fluidId = Registries.FLUID.getId(fluid);
			var existingValue = resolvedFluidValues.get(fluidId);

			if (existingValue != null && existingValue <= perBucketValue) {
				continue;
			}

			resolvedFluidValues.put(fluidId, perBucketValue);

			var bucketItem = fluid.getBucketItem();

			if (bucketItem != null && bucketItem != Items.AIR) {
				var bucketItemId = Registries.ITEM.getId(bucketItem);
				var emptyBucketValue = resolvedValues.getOrDefault(Registries.ITEM.getId(Items.BUCKET), 0);
				var bucketValue = emptyBucketValue + perBucketValue;

				if (applyResolvedItemValue(bucketItemId, bucketValue, resolvedValues, lockedItemIds, discoveredItemIds)) {
					didUpdate = true;
				}
			}

			didUpdate = true;
		}

		return didUpdate;
	}

	/**
	 * Seeds the fluid value cache using any known filled bucket items.
	 *
	 * Helps bootstrap fluid pricing so fluid-only recipes have reference points 
	 * even before explicit fluid outputs are discovered.
	 */
	private static void seedFluidValuesFromBuckets(Map<Identifier, Integer> resolvedValues, Map<Identifier, Integer> resolvedFluidValues) {
		var emptyBucketId = Registries.ITEM.getId(Items.BUCKET);
		var emptyBucketValue = resolvedValues.getOrDefault(emptyBucketId, 0);

		for (var entry : resolvedValues.entrySet()) {
			var item = Registries.ITEM.get(entry.getKey());

			if (!(item instanceof BucketItem bucketItem)) {
				continue;
			}

			var fluid = ((BucketItemAccessor) (Object) bucketItem).commercialize$getFluid();

			if (fluid == null || fluid == Fluids.EMPTY) {
				continue;
			}

			var fluidId = Registries.FLUID.getId(fluid);
			var fluidValue = entry.getValue() - emptyBucketValue;

			if (fluidValue < 0) {
				fluidValue = 0;
			}

			var existingValue = resolvedFluidValues.get(fluidId);

			if (existingValue == null || existingValue > fluidValue) {
				resolvedFluidValues.put(fluidId, fluidValue);
			}
		}
	}

	// Analysis

	/**
	 * Counts how many recipe outputs still lack resolved values after discovery.
	 *
	 * Provides diagnostic logging so missing presets or unsupported recipes 
	 * can be identified and addressed.
	 */
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

	// Utility

	/**
	 * Returns whether the given recipe type should be considered for valuation.
	 *
	 * Uses preset data to skip unsupported or circular recipe chains so 
	 * discovery stays safe and performant.
	 */
	private static boolean isSupportedRecipeType(Identifier recipeTypeId) {
		return ItemValueDiscoveryPresets.isSupportedRecipeType(recipeTypeId);
	}

	/**
	 * Fetches the effort bonus assigned to a recipe type.
	 *
	 * Differentiates resource-intensive automation steps from trivial 
	 * crafting for more realistic pricing.
	 */
	private static int getRecipeEffortValueForType(Identifier recipeTypeId) {
		return ItemValueDiscoveryPresets.getRecipeEffortValue(recipeTypeId);
	}

}
