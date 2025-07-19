package net.saint.commercialize.screen.shipping;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.Containers;

public class ShippingBlockScreen extends BaseOwoHandledScreen<FlowLayout, ShippingBlockScreenHandler> {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_block_screen");

	// Init

	public ShippingBlockScreen(ShippingBlockScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
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

	// Root

	@Override
	protected void build(FlowLayout rootComponent) {
		var wrapperComponent = Containers.verticalFlow(Sizing.fixed(210), Sizing.fixed(177));

		var textureComponent = Components.texture(ShippingBlockScreenAssets.PANEL);
		textureComponent.sizing(Sizing.fixed(210), Sizing.fixed(177));
		textureComponent.positioning(Positioning.absolute(0, 0));
		wrapperComponent.child(textureComponent);

		// …

		rootComponent.child(wrapperComponent);
		rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		rootComponent.id("shipping_block_screen");
	}

}
