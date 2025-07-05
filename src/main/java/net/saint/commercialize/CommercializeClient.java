package net.saint.commercialize;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.saint.commercialize.init.ModClientNetworking;

public class CommercializeClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ModClientNetworking.initialize();
		});
	}

}