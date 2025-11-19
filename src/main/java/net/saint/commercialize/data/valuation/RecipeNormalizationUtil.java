package net.saint.commercialize.data.valuation;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public final class RecipeNormalizationUtil {

	// TODO: Check where Farmer's Delight cooking or Farmer's Respite brewing recipes go.

	/**
	 * Converts any supported recipe into a shared structure describing its inputs and outputs.
	 *
	 * Extracts item ingredients, optional fluid ingredients, and resulting stacks or fluids so 
	 * later logic can stay agnostic of recipe subclasses.
	 */
	public static Optional<NormalizedItemRecipe> normalizeRecipe(Recipe<?> recipe, DynamicRegistryManager registryManager) {
		var recipeType = Registries.RECIPE_TYPE.getId(recipe.getType());

		if (recipe instanceof ProcessingRecipe<?> processingRecipe) {
			return normalizeProcessingRecipe(processingRecipe, recipeType, registryManager);
		} else {
			return normalizeVanillaRecipe(recipe, recipeType, registryManager);
		}
	}

	// Processing Recipe (e.g. Create)

	private static Optional<NormalizedItemRecipe> normalizeProcessingRecipe(ProcessingRecipe<?> processingRecipe, Identifier recipeType,
			DynamicRegistryManager registryManager) {
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

		return Optional.of(
				new NormalizedItemRecipe(
						recipeType,
						itemIngredients,
						fluidIngredients,
						outputStack,
						fluidOutputs
				)
		);
	}

	// Vanilla Recipe

	private static Optional<NormalizedItemRecipe> normalizeVanillaRecipe(Recipe<?> recipe, Identifier recipeType,
			DynamicRegistryManager registryManager) {
		var itemIngredients = compactListFromIngredients(recipe.getIngredients());
		var outputStack = recipe.getOutput(registryManager);

		if (itemIngredients.isEmpty() || outputStack.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(
				new NormalizedItemRecipe(
						recipeType,
						itemIngredients,
						List.of(),
						outputStack,
						List.of()
				)
		);
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
