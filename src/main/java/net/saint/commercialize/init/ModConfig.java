package net.saint.commercialize.init;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.util.ConfigDefaultsUtil;
import net.saint.commercialize.util.ConfigLoadUtil;

public final class ModConfig {

	public static void assertConfigStructure(ResourceManager resourceManager) {
		try {
			// Create directories if they don't already exist.
			Files.createDirectories(Commercialize.MOD_CONFIG_DIR);

			// Check if directory is empty, if so, copy over all defaults.
			if (Files.list(Commercialize.MOD_CONFIG_DIR).findAny().isEmpty()) {
				Commercialize.LOGGER.info("Config directory '{}' is empty, copying default config files.", Commercialize.MOD_CONFIG_DIR);
				ConfigDefaultsUtil.copyAllDefaultConfigs(resourceManager);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not create config directory or copy default config files.", e);
		}
	}

	public static void reloadItemConfigs() {
		Commercialize.ITEM_MANAGER.clearItemValues();
		var itemPresets = ConfigLoadUtil.loadItemConfigs();

		itemPresets.forEach(config -> {
			config.values.forEach((item, value) -> {
				Commercialize.ITEM_MANAGER.registerItemValue(item, value);
			});
		});

		Commercialize.LOGGER.info("Loaded {} item preset(s) with a total of {} item(s).", itemPresets.size(),
				Commercialize.ITEM_MANAGER.size());
	}

	public static void reloadPlayerConfigs() {
		var playersConfig = ConfigLoadUtil.loadPlayerConfigs();

		Commercialize.PLAYER_TEMPLATE_MANAGER.clearReferencePlayerNames();
		Commercialize.PLAYER_TEMPLATE_MANAGER.registerReferencePlayerNames(playersConfig.players);

		Commercialize.LOGGER.info("Loaded {} mock player preset(s).", Commercialize.PLAYER_TEMPLATE_MANAGER.numberOfReferencePlayerNames());
	}

	public static void reloadOfferTemplateConfigs() {
		Commercialize.OFFER_TEMPLATE_MANAGER.clearTemplates();

		var offerTemplates = ConfigLoadUtil.loadOfferTemplateConfigs();
		var numberOfLoadedTemplates = new AtomicInteger(0);
		var numberOfSkippedTemplates = new AtomicInteger(0);

		offerTemplates.forEach(entry -> {
			entry.offers.forEach((offerTemplate) -> {
				if (!Registries.ITEM.containsId(offerTemplate.item)) {
					numberOfSkippedTemplates.getAndIncrement();
					return;
				}

				Commercialize.OFFER_TEMPLATE_MANAGER.registerTemplate(offerTemplate);
				numberOfLoadedTemplates.getAndIncrement();
			});
		});

		Commercialize.LOGGER.info("Loaded offer templates from {} config file(s). Registered a total of {} offer(s) ({} skipped).",
				offerTemplates.size(), numberOfLoadedTemplates.get(), numberOfSkippedTemplates.get());
	}

}
