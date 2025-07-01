package net.saint.commercialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.saint.commercialize.data.item.ItemManager;
import net.saint.commercialize.data.market.MarketManager;
import net.saint.commercialize.data.market.MarketOfferGenerator;
import net.saint.commercialize.data.offer.OfferTemplateManager;
import net.saint.commercialize.init.Blocks;
import net.saint.commercialize.util.ConfigLoadUtil;
import net.saint.commercialize.util.PlayerProfileManager;

public class Commercialize implements ModInitializer {

	// Properties

	public static final String MOD_ID = "commercialize";

	public static final PlayerProfileManager PLAYER_PROFILE_MANAGER = new PlayerProfileManager();

	public static final ItemManager ITEM_MANAGER = new ItemManager();
	public static final OfferTemplateManager OFFER_TEMPLATE_MANAGER = new OfferTemplateManager();

	public static final MarketManager MARKET_MANAGER = new MarketManager();

	// References

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Init

	@Override
	public void onInitialize() {
		Blocks.initialize();

		var itemConfigs = ConfigLoadUtil.loadItemConfigs();

		itemConfigs.forEach(config -> {
			config.values.forEach((item, value) -> {
				ITEM_MANAGER.registerItemValue(item, value);
			});
		});

		Commercialize.LOGGER.info("Loaded {} item configs with a total of {} item(s).", itemConfigs.size(), ITEM_MANAGER.size());

		var offerTemplateConfigs = ConfigLoadUtil.loadOfferTemplateConfigs();

		offerTemplateConfigs.forEach(config -> {
			config.offers.forEach((offerTemplate) -> {
				OFFER_TEMPLATE_MANAGER.registerTemplate(offerTemplate);
			});
		});

		Commercialize.LOGGER.info("Loaded {} offer template configs with a total of {} offer(s).", offerTemplateConfigs.size(),
				OFFER_TEMPLATE_MANAGER.size());

		var playersConfig = ConfigLoadUtil.loadPlayersConfig();
		PLAYER_PROFILE_MANAGER.registerReferencePlayerNames(playersConfig.players);

		Commercialize.LOGGER.info("Loaded {} mock player names.", PLAYER_PROFILE_MANAGER.numberOfReferencePlayerNames());

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			var world = server.getOverworld();

			if (world == null) {
				return;
			}

			MARKET_MANAGER.clearOffers();

			for (int i = 0; i < 4; i++) {
				var offer = MarketOfferGenerator.generateOffer(world);

				if (offer == null) {
					continue;
				}

				MARKET_MANAGER.addOffer(offer);
			}

			Commercialize.LOGGER.info("Generated {} market offer(s) for world '{}'.", MARKET_MANAGER.size(),
					world.getRegistryKey().getValue());
		});

	}
}