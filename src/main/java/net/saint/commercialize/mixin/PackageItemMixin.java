package net.saint.commercialize.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mrcrayfish.furniture.refurbished.item.PackageItem;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.saint.commercialize.mixinlogic.PackageItemMixinLogic;

@Mixin(PackageItem.class)
public abstract class PackageItemMixin implements PackageItemMixinLogic {

	// Tooltip

	@Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
	private void commercialize$_appendTooltip(ItemStack stack, World world, List<Text> lines, TooltipContext context,
			CallbackInfo callbackInfo) {
		commercialize$appendTooltip(stack, world, lines, context, callbackInfo);
	}

}
