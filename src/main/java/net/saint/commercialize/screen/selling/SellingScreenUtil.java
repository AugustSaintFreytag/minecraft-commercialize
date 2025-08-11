package net.saint.commercialize.screen.selling;

import java.util.ArrayList;
import java.util.List;

import net.saint.commercialize.data.text.TextFormattingUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;
import net.saint.commercialize.data.text.TimePreset;
import net.saint.commercialize.gui.common.SelectDropdownComponent;
import net.saint.commercialize.util.LocalizationUtil;

public final class SellingScreenUtil {

	public static List<SelectDropdownComponent.Option<Long>> offerDurationDropdownOptions() {
		var options = new ArrayList<SelectDropdownComponent.Option<Long>>();
		var presets = new long[] { TimePreset.TWELVE_HOURS, TimePreset.ONE_DAY, TimePreset.THREE_DAYS, TimePreset.FIVE_DAYS,
				TimePreset.ONE_WEEK };

		for (var preset : presets) {
			options.add(new SelectDropdownComponent.Option<Long>(preset,
					TextFormattingUtil.capitalizedString(TimeFormattingUtil.formattedTime(preset))));
		}

		return options;
	}

	public static List<SelectDropdownComponent.Option<SellingPostStrategy>> offerPostAsDropdownOptions() {
		var options = new ArrayList<SelectDropdownComponent.Option<SellingPostStrategy>>();

		options.add(new SelectDropdownComponent.Option<SellingPostStrategy>(SellingPostStrategy.AS_STACK,
				LocalizationUtil.localizedString("gui", "selling.post_as.stack")));

		options.add(new SelectDropdownComponent.Option<SellingPostStrategy>(SellingPostStrategy.AS_ITEMS,
				LocalizationUtil.localizedString("gui", "selling.post_as.items")));

		return options;
	}

}
