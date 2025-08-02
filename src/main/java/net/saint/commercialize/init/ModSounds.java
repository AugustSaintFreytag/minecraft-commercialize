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

	public static final SoundEvent MARKET_OPEN_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "market_open"));
	public static final SoundEvent MARKET_CLOSE_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "market_close"));
	public static final SoundEvent CARD_INSERT_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "card_insert"));
	public static final SoundEvent CARD_EJECT_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "card_eject"));

	public static final SoundEvent SHIPPING_OPEN_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "shipping_open"));
	public static final SoundEvent SHIPPING_CLOSE_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "shipping_close"));

	public static final SoundEvent MAILBOX_DELIVERY_SOUND = SoundEvent.of(new Identifier(Commercialize.MOD_ID, "mailbox_delivery"));

	// Init

	public static void initialize() {
		Registry.register(Registries.SOUND_EVENT, OFFER_SELECT_SOUND.getId(), OFFER_SELECT_SOUND);
		Registry.register(Registries.SOUND_EVENT, ORDER_CONFIRM_SOUND.getId(), ORDER_CONFIRM_SOUND);

		Registry.register(Registries.SOUND_EVENT, MARKET_OPEN_SOUND.getId(), MARKET_OPEN_SOUND);
		Registry.register(Registries.SOUND_EVENT, MARKET_CLOSE_SOUND.getId(), MARKET_CLOSE_SOUND);
		Registry.register(Registries.SOUND_EVENT, CARD_INSERT_SOUND.getId(), CARD_INSERT_SOUND);
		Registry.register(Registries.SOUND_EVENT, CARD_EJECT_SOUND.getId(), CARD_EJECT_SOUND);

		Registry.register(Registries.SOUND_EVENT, SHIPPING_OPEN_SOUND.getId(), SHIPPING_OPEN_SOUND);
		Registry.register(Registries.SOUND_EVENT, SHIPPING_CLOSE_SOUND.getId(), SHIPPING_CLOSE_SOUND);

		Registry.register(Registries.SOUND_EVENT, MAILBOX_DELIVERY_SOUND.getId(), MAILBOX_DELIVERY_SOUND);
	}

}
