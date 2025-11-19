package net.saint.commercialize.data.valuation;

import java.util.List;

import com.simibubi.create.foundation.fluid.FluidIngredient;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

public record NormalizedItemRecipe(
		Identifier recipeType,
		List<Ingredient> itemIngredients,
		List<FluidIngredient> fluidIngredients,
		ItemStack itemOutput,
		List<FluidStack> fluidOutputs) {

}
