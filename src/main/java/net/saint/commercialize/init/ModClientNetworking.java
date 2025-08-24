package net.saint.commercialize.init;

import net.saint.commercialize.block.market.MarketBlockClientNetworking;
import net.saint.commercialize.data.item.ItemManagerNetworking;

public final class ModClientNetworking {

	public static void initialize() {
		ItemManagerNetworking.initialize();
		MarketBlockClientNetworking.initialize();
	}

}
