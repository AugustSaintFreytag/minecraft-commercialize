package net.saint.commercialize.gui.market;

import java.util.List;
import java.util.function.Consumer;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.assets.MarketAssets;

public class CartListComponent extends FlowLayout {

	// Properties

	protected ItemStack itemStack;
	protected Text itemDescription;
	protected Text priceDescription;
	protected List<TooltipComponent> offerTooltip;
	protected Consumer<CartListComponent> onPress;

	// Init

	public CartListComponent(ItemStack itemStack, Text itemDescription, Text priceDescription, List<TooltipComponent> offerTooltip,
			Consumer<CartListComponent> onPress) {
		super(Sizing.fixed(134), Sizing.fixed(18), FlowLayout.Algorithm.VERTICAL);

		this.itemStack = itemStack;
		this.itemDescription = itemDescription;
		this.priceDescription = priceDescription;
		this.offerTooltip = offerTooltip;
		this.onPress = onPress;

		var textureComponent = Components.texture(MarketAssets.CART_LIST_ITEM);
		textureComponent.positioning(Positioning.absolute(0, 0));
		textureComponent.sizing(Sizing.fixed(133), Sizing.fixed(18));
		this.child(textureComponent);

		var itemComponent = Components.item(this.itemStack);
		itemComponent.showOverlay(true);
		itemComponent.positioning(Positioning.absolute(2, 0));
		itemComponent.setTooltipFromStack(true);
		this.child(itemComponent);

		var itemDescriptionLabel = Components.label(this.itemDescription);
		itemDescriptionLabel.positioning(Positioning.absolute(28, 5));
		itemDescriptionLabel.sizing(Sizing.fixed(46), Sizing.fixed(12));
		this.child(itemDescriptionLabel);

		var priceDescriptionLabel = Components.label(this.priceDescription).horizontalTextAlignment(HorizontalAlignment.RIGHT);
		priceDescriptionLabel.positioning(Positioning.absolute(76, 5));
		priceDescriptionLabel.sizing(Sizing.fixed(53), Sizing.fixed(12));
		this.child(priceDescriptionLabel);

		var tooltipOverlay = Components.box(Sizing.fixed(108), Sizing.fixed(18));
		tooltipOverlay.color(Color.ofArgb(0x00000020));
		tooltipOverlay.positioning(Positioning.absolute(26, 0));
		tooltipOverlay.tooltip(this.offerTooltip);
		this.child(tooltipOverlay);
	}

	@Override
	public boolean onMouseDown(double mouseX, double mouseY, int button) {
		this.onPress.accept(this);
		return super.onMouseDown(mouseX, mouseY, button);
	}

}
