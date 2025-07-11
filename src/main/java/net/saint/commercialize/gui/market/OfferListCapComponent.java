package net.saint.commercialize.gui.market;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.assets.MarketAssets;
import net.saint.commercialize.util.LocalizationUtil;

public class OfferListCapComponent extends FlowLayout {

	// Init

	public OfferListCapComponent() {
		super(Sizing.fixed(167), Sizing.fixed(18), FlowLayout.Algorithm.VERTICAL);

		var textureComponent = Components.texture(MarketAssets.OFFER_CAP_LIST_ITEM);
		textureComponent.positioning(Positioning.absolute(0, 0));
		textureComponent.sizing(Sizing.fixed(166), Sizing.fixed(18));
		this.child(textureComponent);

		var labelComponent = Components.label(LocalizationUtil.localizedText("gui", "market.offers_cap"));
		labelComponent.color(Color.ofRgb(0x5A5A5A));
		labelComponent.positioning(Positioning.absolute(4, 5));
		labelComponent.sizing(Sizing.fixed(162), Sizing.fixed(12));
		labelComponent.horizontalTextAlignment(HorizontalAlignment.LEFT);
		this.child(labelComponent);
	}

}
