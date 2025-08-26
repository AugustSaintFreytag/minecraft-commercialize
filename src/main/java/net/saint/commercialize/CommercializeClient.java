package net.saint.commercialize;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.init.ModClientNetworking;
import net.saint.commercialize.init.ModScreens;

public class CommercializeClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModScreens.initialize();
		ModBlocks.initializeRenderLayers();

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ModClientNetworking.initialize();
		});
	}

}