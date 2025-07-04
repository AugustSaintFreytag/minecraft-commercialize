package net.saint.commercialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.World;
import net.saint.commercialize.data.item.ItemManager;
import net.saint.commercialize.data.market.MarketManager;
import net.saint.commercialize.data.market.MarketOfferGenerator;
import net.saint.commercialize.data.offer.OfferTemplateManager;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.util.ConfigLoadUtil;
import net.saint.commercialize.util.PlayerProfileManager;

public class Commercialize implements ModInitializer {

	// Configuration

	public static final String MOD_NAME = "Commercialize";
	public static final String MOD_ID = "commercialize";

	// Properties

	public static final ItemManager ITEM_MANAGER = new ItemManager();
	public static final OfferTemplateManager OFFER_TEMPLATE_MANAGER = new OfferTemplateManager();
	public static final PlayerProfileManager PLAYER_PROFILE_MANAGER = new PlayerProfileManager();
	public static final MarketManager MARKET_MANAGER = new MarketManager();

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	// Init

	@Override
	public void onInitialize() {
		ModBlocks.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			loadItemConfigs();
			loadPlayersConfig();
			loadOfferTemplatesConfigs();

			generateDemoOffers(server.getOverworld());
		});

	}

	@Environment(EnvType.SERVER)
	private void loadItemConfigs() {
		var itemConfigs = ConfigLoadUtil.loadItemConfigs();

		itemConfigs.forEach(config -> {
			config.values.forEach((item, value) -> {
				ITEM_MANAGER.registerItemValue(item, value);
			});
		});

		Commercialize.LOGGER.info("Loaded {} item configs with a total of {} item(s).", itemConfigs.size(), ITEM_MANAGER.size());
	}

	@Environment(EnvType.SERVER)
	private void loadPlayersConfig() {
		var playersConfig = ConfigLoadUtil.loadPlayersConfig();
		PLAYER_PROFILE_MANAGER.registerReferencePlayerNames(playersConfig.players);

		Commercialize.LOGGER.info("Loaded {} mock player names.", PLAYER_PROFILE_MANAGER.numberOfReferencePlayerNames());
	}

	@Environment(EnvType.SERVER)
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

	@Environment(EnvType.SERVER)
	private void generateDemoOffers(World world) {
		MARKET_MANAGER.clearOffers();

		for (int i = 0; i < 32; i++) {
			var offer = MarketOfferGenerator.generateOffer(world);

			if (offer.isEmpty()) {
				continue;
			}

			MARKET_MANAGER.addOffer(offer.get());
		}

		Commercialize.LOGGER.info("Generated {} market offer(s) for world '{}'.", MARKET_MANAGER.size(), world.getRegistryKey().getValue());
	}
}