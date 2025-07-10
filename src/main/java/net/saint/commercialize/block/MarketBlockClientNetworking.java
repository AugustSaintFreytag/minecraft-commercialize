package net.saint.commercialize.block;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.network.MarketS2CListMessage;
import net.saint.commercialize.network.MarketS2COrderMessage;

public final class MarketBlockClientNetworking {

	// Init

	public static void initialize() {
		ClientPlayNetworking.registerReceiver(MarketS2CListMessage.ID, (client, handler, buffer, responseSender) -> {
			try {
				var message = MarketS2CListMessage.decodeFromBuffer(buffer);

				client.execute(() -> {
					var blockPosition = message.position;
					var blockEntity = client.world.getBlockEntity(blockPosition);

					if (!(blockEntity instanceof MarketBlockEntity marketBlockEntity)) {
						Commercialize.LOGGER.error(
								"Received market data for block entity at position '{}', but the block entity is not of type 'MarketBlockEntity'.",
								blockPosition.toShortString());
						return;
					}

					marketBlockEntity.receiveListMessage(message);
				});
			} catch (Exception e) {
				Commercialize.LOGGER.error("Could not decode and forward market data received from server.", e);
				return;
			}
		});

		ClientPlayNetworking.registerReceiver(MarketS2COrderMessage.ID, (client, handler, buffer, responseSender) -> {
			try {
				var message = MarketS2COrderMessage.decodeFromBuffer(buffer);

				client.execute(() -> {
					var blockEntity = client.world.getBlockEntity(message.position);
					if (!(blockEntity instanceof MarketBlockEntity marketBlockEntity)) {
						Commercialize.LOGGER.error(
								"Received market order response for block entity at position '{}', but the block entity is not of type 'MarketBlockEntity'.",
								message.position.toShortString());
						return;
					}

					marketBlockEntity.receiveOrderMessage(message);
				});
			} catch (Exception e) {
				Commercialize.LOGGER.error("Could not decode and forward market order response received from server.", e);
			}
		});
	}

}
