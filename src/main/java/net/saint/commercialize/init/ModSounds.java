package net.saint.commercialize.init;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;

public class ModSounds {

	// Sounds

	public static final SoundEvent OFFER_SELECT_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "offer_select"));
	public static final SoundEvent ORDER_CONFIRM_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "order_confirm"));
	public static final SoundEvent MAILBOX_DELIVERY_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "mailbox_delivery"));

	// Init

	public static void initialize() {
		Registry.register(Registries.SOUND_EVENT, OFFER_SELECT_SOUND.getId(), OFFER_SELECT_SOUND);
		Registry.register(Registries.SOUND_EVENT, ORDER_CONFIRM_SOUND.getId(), ORDER_CONFIRM_SOUND);
		Registry.register(Registries.SOUND_EVENT, MAILBOX_DELIVERY_SOUND.getId(), MAILBOX_DELIVERY_SOUND);
	}

}
