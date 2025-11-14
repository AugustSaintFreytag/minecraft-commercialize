package net.saint.commercialize.screen.shipping;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.Containers;
import net.saint.commercialize.screen.market.components.CurrencyDisplayComponent;
import net.saint.commercialize.util.localization.LocalizationUtil;

public class ShippingScreen extends BaseOwoHandledScreen<FlowLayout, ShippingScreenHandler> {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_screen");

	// Init

	public ShippingScreen(ShippingScreenHandler handler, PlayerInventory playerInventory, Text title) {
		super(handler, playerInventory, title);
	}

	// Internals

	@Override
	protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// Update

	public void updateDisplay() {
		var rootComponent = this.uiAdapter.rootComponent;

		var valueDisplay = rootComponent.childById(CurrencyDisplayComponent.class, "value_display");
		valueDisplay.text(ShippingScreenUtil.textForSaleValueForInventory(handler.blockInventory));

		var saleDisplay = rootComponent.childById(LabelComponent.class, "sale_display");
		saleDisplay.text(ShippingScreenUtil.textForNextShippingTime(client.world));
	}

	// Set-Up

	@Override
	protected void init() {
		super.init();

		handler.blockInventory.addListener(inventory -> {
			this.updateDisplay();
		});

		// Invoke post-init in screen handler to set up delegate and references.
		handler.onOpened(this, handler.player());

		this.updateDisplay();
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		var wrapperComponent = Containers.verticalFlow(Sizing.fixed(210), Sizing.fixed(177));

		var textureComponent = Components.texture(ShippingScreenAssets.PANEL);
		textureComponent.sizing(Sizing.fixed(210), Sizing.fixed(177));
		textureComponent.positioning(Positioning.absolute(0, 0));
		wrapperComponent.child(textureComponent);

		var valueLabel = Components.label(LocalizationUtil.localizedText("gui", "shipping.value"));
		valueLabel.positioning(Positioning.absolute(26, 46));
		valueLabel.sizing(Sizing.fixed(35), Sizing.fixed(10));
		valueLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(valueLabel);

		var valueDisplay = new CurrencyDisplayComponent(LocalizationUtil.localizedText("text", "no_value"),
				CurrencyDisplayComponent.Appearance.NEUTRAL);
		valueDisplay.id("value_display");
		valueDisplay.tooltip(LocalizationUtil.localizedText("gui", "shipping.value.tooltip"));
		valueDisplay.positioning(Positioning.absolute(87, 44));
		valueDisplay.sizing(Sizing.fixed(101), Sizing.fixed(13));
		wrapperComponent.child(valueDisplay);

		var saleDisplay = Components.label(LocalizationUtil.localizedText("text", "no_value"));
		saleDisplay.id("sale_display");
		saleDisplay.horizontalTextAlignment(HorizontalAlignment.CENTER);
		saleDisplay.positioning(Positioning.absolute(32, 71));
		saleDisplay.sizing(Sizing.fixed(149), Sizing.fixed(8));
		saleDisplay.color(Color.ofRgb(0x6f6f6f)); // #6f6f6fff
		wrapperComponent.child(saleDisplay);

		rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
		rootComponent.child(wrapperComponent);
	}

}
