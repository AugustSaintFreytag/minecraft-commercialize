package net.saint.commercialize.screen.posting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.item.AbbreviatableItemDescriptionUtil;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;
import net.saint.commercialize.data.text.TextFormattingUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;
import net.saint.commercialize.data.text.TimePreset;
import net.saint.commercialize.gui.common.SelectDropdownComponent;
import net.saint.commercialize.screen.market.components.CurrencyDisplayComponent;
import net.saint.commercialize.util.LocalizationUtil;

public final class PostingScreenUtil {

	private static final int MAX_ITEM_NAME_LENGTH = 16;

	// Item Stack

	public static Text descriptionForItemStack(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return LocalizationUtil.localizedText("text", "no_value");
		}

		return AbbreviatableItemDescriptionUtil.abbreviatableTextForItemStackWithCount(itemStack, MAX_ITEM_NAME_LENGTH);
	}

	public static Text descriptionForItemOfferPrice(int offerPrice) {
		if (offerPrice <= 0) {
			return LocalizationUtil.localizedText("text", "no_value");
		}

		return CurrencyFormattingUtil.currencyText(offerPrice);
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
		var presets = new long[] { TimePreset.oneHour(), TimePreset.twelveHours(), TimePreset.oneDay(), TimePreset.threeDays(),
				TimePreset.fiveDays(), TimePreset.oneWeek(), TimePreset.twoWeeks() };

		for (var preset : presets) {
			options.add(
					new SelectDropdownComponent.Option<Long>(
							preset,
							TextFormattingUtil.capitalizedString(TimeFormattingUtil.formattedTime(preset).getString())
					)
			);
		}

		return options;
	}

	public static List<SelectDropdownComponent.Option<OfferPostStrategy>> offerPostAsDropdownOptions() {
		var options = new ArrayList<SelectDropdownComponent.Option<OfferPostStrategy>>();

		options.add(
				new SelectDropdownComponent.Option<OfferPostStrategy>(
						OfferPostStrategy.AS_STACK,
						LocalizationUtil.localizedString("gui", "posting.post_as.stack")
				)
		);

		options.add(
				new SelectDropdownComponent.Option<OfferPostStrategy>(
						OfferPostStrategy.AS_ITEMS,
						LocalizationUtil.localizedString("gui", "posting.post_as.items")
				)
		);

		return options;
	}

	// Fees

	public static Text descriptionForPostingFees(int fees) {
		if (fees <= 0) {
			return LocalizationUtil.localizedText("text", "no_value");
		}

		return Text.of(CurrencyFormattingUtil.currencyString(fees));
	}

	public static CurrencyDisplayComponent.Appearance appearanceForPostingFees(int fees, boolean canAfford) {
		if (fees == 0) {
			return CurrencyDisplayComponent.Appearance.NEUTRAL;
		}

		if (canAfford) {
			return CurrencyDisplayComponent.Appearance.POSITIVE;
		} else {
			return CurrencyDisplayComponent.Appearance.NEGATIVE;
		}
	}

}
