package net.saint.commercialize.screen.posting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;
import net.saint.commercialize.data.text.ItemDescriptionUtil;
import net.saint.commercialize.data.text.TextFormattingUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;
import net.saint.commercialize.data.text.TimePreset;
import net.saint.commercialize.gui.common.SelectDropdownComponent;
import net.saint.commercialize.util.LocalizationUtil;

public final class PostingScreenUtil {

	// Item Stack

	public static Text descriptionForItemStack(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return LocalizationUtil.localizedText("text", "no_value");
		}

		return Text.of(ItemDescriptionUtil.descriptionForItemStack(itemStack));
	}

	public static Text descriptionForItemOfferPrice(int offerPrice) {
		if (offerPrice <= 0) {
			return LocalizationUtil.localizedText("text", "no_value");
		}

		return Text.of(CurrencyFormattingUtil.formatCurrency(offerPrice));
	}

	// Price

	public static int basePriceForItemStack(ItemStack itemStack, OfferPostStrategy strategy) {
		if (itemStack.isEmpty()) {
			return 0;
		}

		var itemIdentifier = Registries.ITEM.getId(itemStack.getItem());
		var itemBaseValue = Commercialize.ITEM_MANAGER.getValueForItem(itemIdentifier);

		if (strategy == OfferPostStrategy.AS_STACK) {
			return itemBaseValue * itemStack.getCount();
		} else {
			return itemBaseValue;
		}
	}

	// Dropdown Options

	public static List<SelectDropdownComponent.Option<Long>> offerDurationDropdownOptions() {
		var options = new ArrayList<SelectDropdownComponent.Option<Long>>();
		var presets = new long[] { TimePreset.ONE_HOUR, TimePreset.TWELVE_HOURS, TimePreset.ONE_DAY, TimePreset.THREE_DAYS,
				TimePreset.FIVE_DAYS, TimePreset.ONE_WEEK };


		for (var preset : presets) {
			options.add(new SelectDropdownComponent.Option<Long>(preset,
					TextFormattingUtil.capitalizedString(TimeFormattingUtil.formattedTime(preset).getString())));
		}

		return options;
	}

	public static List<SelectDropdownComponent.Option<OfferPostStrategy>> offerPostAsDropdownOptions() {
		var options = new ArrayList<SelectDropdownComponent.Option<OfferPostStrategy>>();

		options.add(new SelectDropdownComponent.Option<OfferPostStrategy>(OfferPostStrategy.AS_STACK,
				LocalizationUtil.localizedString("gui", "posting.post_as.stack")));

		options.add(new SelectDropdownComponent.Option<OfferPostStrategy>(OfferPostStrategy.AS_ITEMS,
				LocalizationUtil.localizedString("gui", "posting.post_as.items")));

		return options;
	}

}
