package net.saint.commercialize.screen.selling;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.text.TimePreset;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.Containers;
import net.saint.commercialize.util.LocalizationUtil;

public class SellingScreen extends BaseOwoHandledScreen<FlowLayout, SellingScreenHandler> {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_screen");

	// Init

	public SellingScreen(SellingScreenHandler handler, PlayerInventory playerInventory, Text title) {
		super(handler, playerInventory, title);

		handler.blockInventory.addListener(inventory -> {
			this.updateDisplay();
		});
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
	}

	// Root

	@Override
	protected void build(FlowLayout rootComponent) {
		// this.uiAdapter.enableInspector = true;

		var wrapperComponent = Containers.verticalFlow(Sizing.fixed(215), Sizing.fixed(215));

		var textureComponent = Components.texture(SellingScreenAssets.PANEL);
		textureComponent.sizing(Sizing.fixed(215), Sizing.fixed(215));
		textureComponent.positioning(Positioning.absolute(0, 0));
		wrapperComponent.child(textureComponent);

		// Item Row

		var itemNameLabel = Components.label(LocalizationUtil.localizedText("gui", "selling.item"));
		itemNameLabel.positioning(Positioning.absolute(13, 44));
		itemNameLabel.sizing(Sizing.fixed(58), Sizing.fixed(8));
		itemNameLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(itemNameLabel);

		var itemNameDisplay = Components.label(LocalizationUtil.localizedText("text", "no_value"));
		itemNameDisplay.id("item_name_display");
		itemNameDisplay.positioning(Positioning.absolute(76, 44));
		itemNameDisplay.sizing(Sizing.fixed(101), Sizing.fixed(8));
		itemNameDisplay.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(itemNameDisplay);

		// Price Row

		var priceLabel = Components.label(LocalizationUtil.localizedText("gui", "selling.price"));
		priceLabel.positioning(Positioning.absolute(13, 68));
		priceLabel.sizing(Sizing.fixed(58), Sizing.fixed(8));
		priceLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(priceLabel);

		var priceDisplay = Components.label(LocalizationUtil.localizedText("text", "no_value"));
		priceDisplay.id("price_display");
		priceDisplay.positioning(Positioning.absolute(77, 68));
		priceDisplay.sizing(Sizing.fixed(97), Sizing.fixed(8));
		priceDisplay.horizontalTextAlignment(HorizontalAlignment.RIGHT);
		priceDisplay.shadow(true);
		wrapperComponent.child(priceDisplay);

		// Duration Row

		var durationLabel = Components.label(LocalizationUtil.localizedText("gui", "selling.duration"));
		durationLabel.positioning(Positioning.absolute(13, 86));
		durationLabel.sizing(Sizing.fixed(58), Sizing.fixed(8));
		durationLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(durationLabel);

		var durationDropdown = Components.selectDropdown(SellingScreenUtil.offerDurationDropdownOptions());
		durationDropdown.id("offer_time_dropdown");
		durationDropdown.positioning(Positioning.absolute(74, 83));
		durationDropdown.sizing(Sizing.fixed(103), Sizing.fill(100));
		durationDropdown.popoverWidth(75);
		durationDropdown.value(TimePreset.THREE_DAYS);
		wrapperComponent.child(durationDropdown);

		// Post As Row

		var postAsLabel = Components.label(LocalizationUtil.localizedText("gui", "selling.post_as"));
		postAsLabel.positioning(Positioning.absolute(13, 104));
		postAsLabel.sizing(Sizing.fixed(58), Sizing.fixed(8));
		postAsLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(postAsLabel);

		var postAsDropdown = Components.selectDropdown(SellingScreenUtil.offerPostAsDropdownOptions());
		postAsDropdown.id("offer_post_as_dropdown");
		postAsDropdown.positioning(Positioning.absolute(74, 101));
		postAsDropdown.sizing(Sizing.fixed(103), Sizing.fill(100));
		postAsDropdown.popoverWidth(95);
		postAsDropdown.value(SellingPostStrategy.AS_STACK);
		wrapperComponent.child(postAsDropdown);

		// Finalize

		rootComponent.child(wrapperComponent);
		rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		rootComponent.id("selling_screen");

		this.updateDisplay();
	}

}
