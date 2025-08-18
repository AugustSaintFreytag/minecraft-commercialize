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
import net.saint.commercialize.util.LocalizationUtil;

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

		var valueDisplay = rootComponent.childById(LabelComponent.class, "value_display");
		valueDisplay.text(ShippingScreenUtil.textForSaleValueForInventory(handler.blockInventory));

		var saleDisplay = rootComponent.childById(LabelComponent.class, "sale_display");
		saleDisplay.text(LocalizationUtil.localizedText("gui", "shipping.sale_time_format",
				ShippingScreenUtil.textForNextShippingTime(client.world)));
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
		valueLabel.positioning(Positioning.absolute(32, 45));
		valueLabel.sizing(Sizing.fixed(35), Sizing.fixed(7));
		valueLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(valueLabel);

		var valueDisplay = Components.label(LocalizationUtil.localizedText("text", "no_value"));
		valueDisplay.id("value_display");
		valueDisplay.horizontalTextAlignment(HorizontalAlignment.RIGHT);
		valueDisplay.positioning(Positioning.absolute(72, 45));
		valueDisplay.sizing(Sizing.fixed(106), Sizing.fixed(11));
		valueDisplay.color(Color.WHITE);
		valueDisplay.shadow(true);
		wrapperComponent.child(valueDisplay);

		var saleLabel = Components.label(LocalizationUtil.localizedText("gui", "shipping.sale"));
		saleLabel.positioning(Positioning.absolute(32, 68));
		saleLabel.sizing(Sizing.fixed(35), Sizing.fixed(7));
		saleLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(saleLabel);

		var saleDisplay = Components.label(LocalizationUtil.localizedText("text", "no_value"));
		saleDisplay.id("sale_display");
		saleDisplay.horizontalTextAlignment(HorizontalAlignment.RIGHT);
		saleDisplay.positioning(Positioning.absolute(70, 68));
		saleDisplay.sizing(Sizing.fixed(111), Sizing.fixed(7));
		saleDisplay.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(saleDisplay);

		rootComponent.id("shipping_block_screen");
		rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
		rootComponent.child(wrapperComponent);
	}

}
