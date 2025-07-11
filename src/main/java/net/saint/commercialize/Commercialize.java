package net.saint.commercialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.saint.commercialize.data.inventory.PlayerInventoryCashUtil;
import net.saint.commercialize.data.item.ItemManager;
import net.saint.commercialize.data.market.MarketOfferCollection;
import net.saint.commercialize.data.market.MarketOfferTickingUtil;
import net.saint.commercialize.data.market.MarketPersistentStorageUtil;
import net.saint.commercialize.data.offer.OfferTemplateManager;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.init.ModCommands;
import net.saint.commercialize.init.ModServerNetworking;
import net.saint.commercialize.init.ModSounds;
import net.saint.commercialize.util.ConfigLoadUtil;
import net.saint.commercialize.util.PlayerProfileManager;

public class Commercialize implements ModInitializer {

	// Configuration

	public static final String MOD_NAME = "Commercialize";
	public static final String MOD_ID = "commercialize";

	// Properties

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static ItemManager ITEM_MANAGER;
	public static OfferTemplateManager OFFER_TEMPLATE_MANAGER;
	public static PlayerProfileManager PLAYER_PROFILE_MANAGER;
	public static MarketOfferCollection MARKET_MANAGER;

	public static boolean shouldTickMarket = true;

	// Init

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModSounds.initialize();
		ModCommands.initialize();
		ModServerNetworking.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ITEM_MANAGER = new ItemManager();
			OFFER_TEMPLATE_MANAGER = new OfferTemplateManager();
			PLAYER_PROFILE_MANAGER = new PlayerProfileManager();
			MARKET_MANAGER = MarketPersistentStorageUtil.loadPersistentMarketManager(server);

			loadItemConfigs();
			loadPlayersConfig();
			loadOfferTemplatesConfigs();

			PlayerInventoryCashUtil.initialize();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!shouldTickMarket) {
				return;
			}

			var world = server.getOverworld();
			MarketOfferTickingUtil.tickMarketOffersIfNecessary(world);
		});

	}

	private void loadItemConfigs() {
		var itemConfigs = ConfigLoadUtil.loadItemConfigs();

		itemConfigs.forEach(config -> {
			config.values.forEach((item, value) -> {
				ITEM_MANAGER.registerItemValue(item, value);
			});
		});

		Commercialize.LOGGER.info("Loaded {} item configs with a total of {} item(s).", itemConfigs.size(), ITEM_MANAGER.size());
	}

	private void loadPlayersConfig() {
		var playersConfig = ConfigLoadUtil.loadPlayersConfig();
		PLAYER_PROFILE_MANAGER.registerReferencePlayerNames(playersConfig.players);

		Commercialize.LOGGER.info("Loaded {} mock player names.", PLAYER_PROFILE_MANAGER.numberOfReferencePlayerNames());
	}

	private void loadOfferTemplatesConfigs() {
		var offerTemplateConfigs = ConfigLoadUtil.loadOfferTemplateConfigs();

		offerTemplateConfigs.forEach(config -> {
			config.offers.forEach((offerTemplate) -> {
				OFFER_TEMPLATE_MANAGER.registerTemplate(offerTemplate);
			});
		});

		Commercialize.LOGGER.info("Loaded {} offer template configs with a total of {} offer(s).", offerTemplateConfigs.size(),
				OFFER_TEMPLATE_MANAGER.size());
	}

}