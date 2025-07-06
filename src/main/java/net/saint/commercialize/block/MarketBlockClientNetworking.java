package net.saint.commercialize.block;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.network.MarketS2CListMessage;

public final class MarketBlockClientNetworking {

	// Init

	public static void initialize() {
		ClientPlayNetworking.registerReceiver(MarketS2CListMessage.ID, (client, handler, buffer, responseSender) -> {
			try {
				var message = MarketS2CListMessage.decodeFromBuffer(buffer);

				client.execute(() -> {
					onReceiveMarketListMessage(client, message);
				});
			} catch (Exception e) {
				Commercialize.LOGGER.error("Could not decode and forward market data received from server.", e);
				return;
			}
		});
	}

	// Handlers

	private static void onReceiveMarketListMessage(MinecraftClient client, MarketS2CListMessage message) {
		var blockPosition = message.position;
		var blockEntity = client.world.getBlockEntity(blockPosition);

		if (!(blockEntity instanceof MarketBlockEntity)) {
			Commercialize.LOGGER.error(
					"Received market data for block entity at position '{}', but the block entity is not of type 'MarketBlockEntity'.",
					blockPosition.toShortString());
		}

		var marketBlockEntity = (MarketBlockEntity) blockEntity;
		marketBlockEntity.receiveServerMessage(message);
	}

}
