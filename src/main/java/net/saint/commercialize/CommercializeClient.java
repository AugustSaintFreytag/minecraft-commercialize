package net.saint.commercialize;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.saint.commercialize.data.item.ItemManager;
import net.saint.commercialize.data.player.PlayerProfileManager;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.init.ModClientNetworking;
import net.saint.commercialize.init.ModScreens;

public class CommercializeClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModScreens.initialize();
		ModBlocks.initializeRenderLayers();

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			Commercialize.ITEM_MANAGER = new ItemManager();
			Commercialize.PLAYER_PROFILE_MANAGER = new PlayerProfileManager(client);
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ModClientNetworking.initialize();
		});
	}

}