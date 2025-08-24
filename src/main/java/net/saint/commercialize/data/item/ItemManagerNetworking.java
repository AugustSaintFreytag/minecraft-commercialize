package net.saint.commercialize.data.item;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.network.ItemRegistryS2CSyncMessage;

public final class ItemManagerNetworking {

	public static void initialize() {
		ClientPlayNetworking.registerReceiver(ItemRegistryS2CSyncMessage.ID, (client, handler, buffer, responseSender) -> {
			try {
				var message = ItemRegistryS2CSyncMessage.decodeFromBuffer(buffer);
				client.execute(() -> {
					Commercialize.ITEM_MANAGER.clearItemValues();

					for (var entry : message.valueByItem.entrySet()) {
						Commercialize.ITEM_MANAGER.registerItemValue(entry.getKey(), entry.getValue());
					}

					Commercialize.LOGGER.info("Synchronized item registry from server with {} entries.",
							message.valueByItem.size());
				});
			} catch (Exception e) {
				Commercialize.LOGGER.error("Could not decode item registry data received from server on client.", e);
			}
		});
	}

	public static void syncItemRegistryToPlayer(ServerPlayerEntity player) {
		var message = new ItemRegistryS2CSyncMessage();
		message.valueByItem = Commercialize.ITEM_MANAGER.getValuesByItem();

		var buffer = PacketByteBufs.create();
		message.encodeToBuffer(buffer);

		ServerPlayNetworking.send(player, ItemRegistryS2CSyncMessage.ID, buffer);
	}

	public static void syncItemRegistryToAllPlayers(MinecraftServer server) {
		for (var player : server.getPlayerManager().getPlayerList()) {
			syncItemRegistryToPlayer(player);
		}
	}
	
}
