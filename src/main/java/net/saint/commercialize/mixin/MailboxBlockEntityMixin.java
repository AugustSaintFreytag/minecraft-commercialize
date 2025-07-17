package net.saint.commercialize.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mrcrayfish.furniture.refurbished.blockentity.MailboxBlockEntity;

import net.minecraft.sound.SoundCategory;
import net.saint.commercialize.init.ModSounds;

@Mixin(com.mrcrayfish.furniture.refurbished.blockentity.MailboxBlockEntity.class)
public abstract class MailboxBlockEntityMixin {

	@Inject(method = "setUnchecked", at = @At("TAIL"), remap = false)
	private void setUnchecked(CallbackInfo callbackInfo) {
		var blockEntity = (MailboxBlockEntity) (Object) this;
		var world = blockEntity.getWorld();

		world.playSound(null, blockEntity.getPos(), ModSounds.MAILBOX_DELIVERY_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
	}

}
