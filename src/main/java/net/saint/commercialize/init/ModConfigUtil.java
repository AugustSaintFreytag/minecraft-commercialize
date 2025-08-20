package net.saint.commercialize.init;

import net.saint.commercialize.Commercialize;
import net.saint.commercialize.util.ConfigLoadUtil;

public final class ModConfigUtil {

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

		Commercialize.PLAYER_PROFILE_MANAGER.clearReferencePlayerNames();
		Commercialize.PLAYER_PROFILE_MANAGER.registerReferencePlayerNames(playersConfig.players);

		Commercialize.LOGGER.info("Loaded {} mock player preset(s).", Commercialize.PLAYER_PROFILE_MANAGER.numberOfReferencePlayerNames());
	}

	public static void reloadOfferTemplateConfigs() {
		Commercialize.OFFER_TEMPLATE_MANAGER.clearTemplates();
		var offerTemplates = ConfigLoadUtil.loadOfferTemplateConfigs();

		offerTemplates.forEach(entry -> {
			entry.offers.forEach((offerTemplate) -> {
				Commercialize.OFFER_TEMPLATE_MANAGER.registerTemplate(offerTemplate);
			});
		});

		Commercialize.LOGGER.info("Loaded {} offer template(s) with a total of {} offer(s).", offerTemplates.size(),
				Commercialize.OFFER_TEMPLATE_MANAGER.size());
	}

}
