package net.saint.commercialize.data.valuation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

public final class RecipeNormalizationUtil {

	/**
	 * Converts any supported recipe into a shared structure describing its inputs and outputs.
	 *
	 * Extracts item ingredients, optional fluid ingredients, and resulting stacks or fluids so 
	 * later logic can stay agnostic of recipe subclasses.
	 */
	public static Optional<NormalizedItemRecipe> normalizeRecipe(Recipe<?> recipe, DynamicRegistryManager registryManager) {
		var recipeType = Registries.RECIPE_TYPE.getId(recipe.getType());

		var output = recipe.getOutput(registryManager);
		if (output != null && output.getTranslationKey().contains("blaze")) {
			var debug = true;
		}

		if (recipe instanceof ProcessingRecipe<?> processingRecipe) {
			return normalizeProcessingRecipe(processingRecipe, recipeType, registryManager);
		}

		if (recipe instanceof SequencedAssemblyRecipe sequencedAssemblyRecipe) {
			return normalizeSequencedAssemblyRecipe(sequencedAssemblyRecipe, recipeType, registryManager);
		}

		if (recipe instanceof CookingPotRecipe) {
			return normalizeCookingPotRecipe((CookingPotRecipe) recipe, recipeType, registryManager);
		}

		return normalizeVanillaRecipe(recipe, recipeType, registryManager);
	}

	// Processing Recipe (e.g. Create)

	private static Optional<NormalizedItemRecipe> normalizeProcessingRecipe(ProcessingRecipe<?> processingRecipe, Identifier recipeType,
			DynamicRegistryManager registryManager) {
		var recipeEffort = NormalizedItemRecipe.Effort.REGULAR;
		var itemIngredients = compactListFromIngredients(processingRecipe.getIngredients());
		var outputStack = processingRecipe.getOutput(registryManager);
		var fluidIngredients = compactListFromFluidIngredients(processingRecipe.getFluidIngredients());
		var fluidOutputs = compactListFromFluidStacks(processingRecipe.getFluidResults());

		if (itemIngredients.isEmpty() && fluidIngredients.isEmpty()) {
			return Optional.empty();
		}

		if (outputStack.isEmpty() && fluidOutputs.isEmpty()) {
			return Optional.empty();
		}

		if (processingRecipe.getRequiredHeat() == HeatCondition.HEATED) {
			recipeEffort = NormalizedItemRecipe.Effort.ELEVATED;
		} else if (processingRecipe.getRequiredHeat() == HeatCondition.SUPERHEATED) {
			recipeEffort = NormalizedItemRecipe.Effort.EXTREME;
		}

		return Optional.of(
				new NormalizedItemRecipe(
						recipeType,
						recipeEffort,
						itemIngredients,
						fluidIngredients,
						outputStack,
						fluidOutputs
				)
		);
	}

	// Cooking Pot Recipe (e.g. Farmer's Delight)

	private static Optional<NormalizedItemRecipe> normalizeCookingPotRecipe(CookingPotRecipe cookingPotRecipe, Identifier recipeType,
			DynamicRegistryManager registryManager) {
		var recipeEffort = NormalizedItemRecipe.Effort.REGULAR;
		var itemIngredients = compactListFromIngredients(cookingPotRecipe.getIngredients());
		var outputStack = cookingPotRecipe.getOutput(registryManager);

		if (itemIngredients.isEmpty() || outputStack.isEmpty()) {
			return Optional.empty();
		}

		var outputContainerStack = cookingPotRecipe.getOutputContainer();

		if (!outputContainerStack.isEmpty()) {
			var outputContainerIngredient = Ingredient.ofStacks(outputContainerStack);

			var augmentedItemIngredients = new ArrayList<>(itemIngredients);
			augmentedItemIngredients.add(outputContainerIngredient);

			itemIngredients = List.copyOf(augmentedItemIngredients);
		}

		return Optional.of(
				new NormalizedItemRecipe(
						recipeType,
						recipeEffort,
						itemIngredients,
						List.of(),
						outputStack,
						List.of()
				)
		);
	}

	// Vanilla Recipe

	private static Optional<NormalizedItemRecipe> normalizeVanillaRecipe(Recipe<?> recipe, Identifier recipeType,
			DynamicRegistryManager registryManager) {
		var recipeEffort = NormalizedItemRecipe.Effort.REGULAR;
		var itemIngredients = compactListFromIngredients(recipe.getIngredients());
		var outputStack = recipe.getOutput(registryManager);

		if (itemIngredients.isEmpty() || outputStack.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(
				new NormalizedItemRecipe(
						recipeType,
						recipeEffort,
						itemIngredients,
						List.of(),
						outputStack,
						List.of()
				)
		);
	}

	// Sequenced Assembly Recipe (Create)

	private static Optional<NormalizedItemRecipe> normalizeSequencedAssemblyRecipe(SequencedAssemblyRecipe sequencedRecipe,
			Identifier recipeType, DynamicRegistryManager registryManager) {
		var recipeEffort = NormalizedItemRecipe.Effort.REGULAR;
		var transitionalStack = sequencedRecipe.getTransitionalItem();
		var collectedItemIngredients = new ArrayList<Ingredient>();
		var baseIngredient = sequencedRecipe.getIngredient();

		if (baseIngredient != null && !baseIngredient.isEmpty()) {
			collectedItemIngredients.add(baseIngredient);
		}

		for (var step : sequencedRecipe.getSequence()) {
			var processingRecipe = step.getRecipe();

			if (processingRecipe == null) {
				continue;
			}

			var stepIngredients = processingRecipe.getIngredients();

			if (stepIngredients == null || stepIngredients.isEmpty()) {
				continue;
			}

			for (var ingredient : stepIngredients) {
				if (ingredient == null || ingredient.isEmpty()) {
					continue;
				}

				if (isTransitionalIngredient(ingredient, transitionalStack)) {
					continue;
				}

				collectedItemIngredients.add(ingredient);
			}
		}

		var normalizedItemIngredients = collectedItemIngredients.stream()
				.filter(Objects::nonNull)
				.filter(ingredient -> !ingredient.isEmpty())
				.toList();

		var fluidIngredients = new ArrayList<FluidIngredient>();
		sequencedRecipe.addAdditionalFluidIngredients(fluidIngredients);

		var normalizedFluidIngredients = fluidIngredients.stream()
				.filter(Objects::nonNull)
				.toList();

		if (normalizedItemIngredients.isEmpty() && normalizedFluidIngredients.isEmpty()) {
			return Optional.empty();
		}

		var outputStack = sequencedRecipe.getOutput(registryManager);

		if (outputStack.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(
				new NormalizedItemRecipe(
						recipeType,
						recipeEffort,
						normalizedItemIngredients,
						normalizedFluidIngredients,
						outputStack,
						List.of()
				)
		);
	}

	private static boolean isTransitionalIngredient(Ingredient ingredient, ItemStack transitionalStack) {
		if (ingredient == null || ingredient.isEmpty() || transitionalStack == null || transitionalStack.isEmpty()) {
			return false;
		}

		var matchingStacks = ingredient.getMatchingStacks();

		if (matchingStacks == null || matchingStacks.length == 0) {
			return false;
		}

		for (var matchingStack : matchingStacks) {
			if (matchingStack == null || matchingStack.isEmpty()) {
				return false;
			}

			if (!ItemStack.canCombine(matchingStack, transitionalStack)) {
				return false;
			}
		}

		return true;
	}

	// Utility

	private static List<Ingredient> compactListFromIngredients(DefaultedList<Ingredient> list) {
		return list.stream()
				.filter(ingredient -> !ingredient.isEmpty())
				.toList();
	}

	private static List<FluidIngredient> compactListFromFluidIngredients(DefaultedList<FluidIngredient> list) {
		return list.stream().toList();
	}

	private static List<FluidStack> compactListFromFluidStacks(DefaultedList<FluidStack> list) {
		return list.stream()
				.filter(fluidStack -> !fluidStack.isEmpty())
				.toList();
	}

}
