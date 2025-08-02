package net.saint.commercialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.saint.commercialize.data.inventory.InventoryAccessUtil;
import net.saint.commercialize.data.item.ItemManager;
import net.saint.commercialize.data.market.MarketOfferCollection;
import net.saint.commercialize.data.market.MarketOfferTickingUtil;
import net.saint.commercialize.data.market.MarketPersistentStorageUtil;
import net.saint.commercialize.data.offer.OfferTemplateManager;
import net.saint.commercialize.data.player.PlayerProfileManager;
import net.saint.commercialize.init.ModBlockEntities;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.init.ModCommands;
import net.saint.commercialize.init.ModConfigUtil;
import net.saint.commercialize.init.ModItems;
import net.saint.commercialize.init.ModScreenHandlers;
import net.saint.commercialize.init.ModServerNetworking;
import net.saint.commercialize.init.ModSounds;

public class Commercialize implements ModInitializer {

	// Configuration

	public static final String MOD_NAME = "Commercialize";
	public static final String MOD_ID = "commercialize";

	// Properties

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static CommercializeConfig CONFIG;

	public static ItemManager ITEM_MANAGER;
	public static OfferTemplateManager OFFER_TEMPLATE_MANAGER;
	public static PlayerProfileManager PLAYER_PROFILE_MANAGER;
	public static MarketOfferCollection MARKET_MANAGER;

	public static boolean shouldTickMarket = true;

	// Init

	@Override
	public void onInitialize() {
		AutoConfig.register(CommercializeConfig.class, JanksonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(CommercializeConfig.class).getConfig();

		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModScreenHandlers.initialize();
		ModSounds.initialize();
		ModCommands.initialize();
		ModServerNetworking.initialize();
		InventoryAccessUtil.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ITEM_MANAGER = new ItemManager();
			OFFER_TEMPLATE_MANAGER = new OfferTemplateManager();
			PLAYER_PROFILE_MANAGER = new PlayerProfileManager();
			MARKET_MANAGER = MarketPersistentStorageUtil.loadPersistentMarketCollection(server);

			reloadConfigs();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!shouldTickMarket) {
				return;
			}

			var world = server.getOverworld();
			MarketOfferTickingUtil.tickMarketOffersIfNecessary(world);
		});

	}

	public static void reloadConfigs() {
		ModConfigUtil.reloadItemConfigs();
		ModConfigUtil.reloadPlayerConfigs();
		ModConfigUtil.reloadOfferTemplateConfigs();
	}

}