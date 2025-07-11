package net.saint.commercialize.data.market;

import net.minecraft.server.MinecraftServer;
import net.saint.commercialize.Commercialize;

public final class MarketPersistentStorageUtil {

	public static MarketOfferCollection loadPersistentMarketManager(MinecraftServer server) {
		var persistentStateManager = server.getOverworld().getPersistentStateManager();
		var marketManager = persistentStateManager.getOrCreate(MarketOfferCollection::fromNbt, MarketOfferCollection::new,
				Commercialize.MOD_ID);

		return marketManager;
	}

}
