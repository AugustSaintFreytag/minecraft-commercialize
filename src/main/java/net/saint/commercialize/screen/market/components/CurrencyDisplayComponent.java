package net.saint.commercialize.screen.market.components;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.common.TextureComponent;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.screen.icons.ScreenAssets;
import net.saint.commercialize.screen.market.MarketScreenAssets;

public class CurrencyDisplayComponent extends FlowLayout {

	// Library

	public static enum Appearance {
		NEUTRAL, POSITIVE, NEGATIVE
	}

	// Properties

	protected Text text;
	protected Appearance appearance;

	public Text text() {
		return this.text;
	}

	public void text(Text text) {
		this.text = text;

		var currencyDisplay = this.childById(LabelComponent.class, "amount");
		currencyDisplay.text(text);
	}

	public Appearance appearance() {
		return this.appearance;
	}

	public void appearance(Appearance highlightStyle) {
		this.appearance = highlightStyle;

		var textureComponent = this.childById(TextureComponent.class, "background");
		textureComponent.reference(backgroundTexture());
	}

	// Init

	public CurrencyDisplayComponent(Text description, Appearance highlightStyle) {
		super(Sizing.fixed(97), Sizing.fixed(13), FlowLayout.Algorithm.VERTICAL);

		this.text = description;
		this.appearance = highlightStyle;

		var textureComponent = Components.texture(backgroundTexture());
		textureComponent.id("background");
		textureComponent.positioning(Positioning.absolute(0, 0));
		textureComponent.sizing(Sizing.fill(100), Sizing.fill(100));
		this.child(textureComponent);

		var currencyDisplay = Components.label(description);
		currencyDisplay.id("amount");
		currencyDisplay.positioning(Positioning.absolute(0, 0));
		currencyDisplay.margins(Insets.both(2, 2));
		currencyDisplay.sizing(Sizing.fill(100), Sizing.fill(100));
		currencyDisplay.horizontalTextAlignment(HorizontalAlignment.RIGHT);

		currencyDisplay.color(Color.WHITE);
		currencyDisplay.shadow(true);
		this.child(currencyDisplay);
	}

	private TextureReference backgroundTexture() {
		switch (this.appearance) {
			case POSITIVE:
				return MarketScreenAssets.POSITIVE_BALANCE_BACKDROP;
			case NEGATIVE:
				return MarketScreenAssets.NEGATIVE_BALANCE_BACKDROP;
			default:
				return ScreenAssets.STUB;
		}
	}

}
