package net.saint.commercialize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTransformRecipe;

@Mixin(SmithingTransformRecipe.class)
public interface SmithingTransformRecipeAccessor {

	@Accessor("template")
	Ingredient commercialize$getTemplate();

	@Accessor("base")
	Ingredient commercialize$getBase();

	@Accessor("addition")
	Ingredient commercialize$getAddition();

}
