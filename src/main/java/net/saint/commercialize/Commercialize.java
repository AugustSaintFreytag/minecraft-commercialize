package net.saint.commercialize;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.saint.commercialize.data.inventory.InventoryAccessUtil;
import net.saint.commercialize.data.item.ItemManager;
import net.saint.commercialize.data.item.ItemManagerNetworking;
import net.saint.commercialize.data.mail.MailTransitManager;
import net.saint.commercialize.data.mail.MailTransitUtil;
import net.saint.commercialize.data.market.MarketOfferCacheManager;
import net.saint.commercialize.data.market.MarketOfferManager;
import net.saint.commercialize.data.market.MarketOfferTickingUtil;
import net.saint.commercialize.data.offer.OfferTemplateManager;
import net.saint.commercialize.data.player.PlayerProfileManager;
import net.saint.commercialize.data.player.PlayerTemplateManager;
import net.saint.commercialize.init.ModBlockEntities;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.init.ModCommands;
import net.saint.commercialize.init.ModConfig;
import net.saint.commercialize.init.ModItems;
import net.saint.commercialize.init.ModScreenHandlers;
import net.saint.commercialize.init.ModServerNetworking;
import net.saint.commercialize.init.ModSounds;
import net.saint.commercialize.util.database.DatabaseManager;

public class Commercialize implements ModInitializer {

	// Configuration

	public static final String MOD_NAME = "Commercialize";
	public static final String MOD_ID = "commercialize";

	// References

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static CommercializeConfig CONFIG;

	public static DatabaseManager DATABASE_MANAGER;
	public static ItemManager ITEM_MANAGER;
	public static OfferTemplateManager OFFER_TEMPLATE_MANAGER;
	public static PlayerTemplateManager PLAYER_TEMPLATE_MANAGER;
	public static PlayerProfileManager PLAYER_PROFILE_MANAGER;
	public static MarketOfferManager MARKET_OFFER_MANAGER;
	public static MarketOfferCacheManager MARKET_OFFER_CACHE_MANAGER;
	public static MailTransitManager MAIL_TRANSIT_MANAGER;

	// Paths

	public static final Path MOD_CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(Commercialize.MOD_ID);

	// State

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

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ITEM_MANAGER = new ItemManager();
			OFFER_TEMPLATE_MANAGER = new OfferTemplateManager();
			PLAYER_TEMPLATE_MANAGER = new PlayerTemplateManager();

			reloadConfigs(server);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MAIL_TRANSIT_MANAGER = MailTransitManager.loadFromServer(server);
			MARKET_OFFER_MANAGER = MarketOfferManager.loadFromServer(server);
			MARKET_OFFER_CACHE_MANAGER = new MarketOfferCacheManager();

			MARKET_OFFER_MANAGER.STATE_MODIFIED.register(manager -> {
				MARKET_OFFER_CACHE_MANAGER.clear();
			});

			DATABASE_MANAGER = new DatabaseManager(server);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!shouldTickMarket) {
				return;
			}

			var world = server.getOverworld();

			MarketOfferTickingUtil.tickMarketOffersIfNecessary(world);
			MailTransitUtil.tickMailTransitIfNecessary(world);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var player = handler.getPlayer();
			ItemManagerNetworking.syncItemRegistryToPlayer(player);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			DATABASE_MANAGER.tearDown();
		});
	}

	public static void reloadConfigs(MinecraftServer server) {
		ModConfig.assertConfigStructure(server.getResourceManager());

		ModConfig.reloadItemConfigs();
		ModConfig.reloadPlayerConfigs();
		ModConfig.reloadOfferTemplateConfigs();

		ItemManagerNetworking.syncItemRegistryToAllPlayers(server);
	}

}