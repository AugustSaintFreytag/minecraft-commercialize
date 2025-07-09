package net.saint.commercialize.gui.market;

import java.util.List;
import java.util.function.Consumer;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.assets.MarketAssets;
import net.saint.commercialize.init.ModSounds;
import net.saint.commercialize.library.TextureReference;

public class OfferListComponent extends FlowLayout {

	// Properties

	protected ItemStack itemStack;
	protected Text itemDescription;
	protected Text priceDescription;
	protected List<TooltipComponent> offerTooltip;
	protected List<TooltipComponent> sellerTooltip;
	protected TextureReference profileTexture;
	protected Consumer<OfferListComponent> onPress;

	// Init

	public OfferListComponent(ItemStack itemStack, Text itemDescription, Text priceDescription, List<TooltipComponent> offerTooltip,
			List<TooltipComponent> sellerTooltip, TextureReference profileTexture, Consumer<OfferListComponent> onPress) {
		super(Sizing.fixed(167), Sizing.fixed(18), FlowLayout.Algorithm.VERTICAL);

		this.itemStack = itemStack;
		this.itemDescription = itemDescription;
		this.priceDescription = priceDescription;
		this.offerTooltip = offerTooltip;
		this.sellerTooltip = sellerTooltip;
		this.profileTexture = profileTexture;
		this.onPress = onPress;

		var textureComponent = Components.texture(MarketAssets.OFFER_LIST_ITEM);
		textureComponent.positioning(Positioning.absolute(0, 0));
		textureComponent.sizing(Sizing.fixed(166), Sizing.fixed(18));
		this.child(textureComponent);

		var itemComponent = Components.item(this.itemStack);
		itemComponent.showOverlay(true);
		itemComponent.positioning(Positioning.absolute(2, 0));
		itemComponent.setTooltipFromStack(true);
		this.child(itemComponent);

		var itemDescriptionLabel = Components.label(this.itemDescription);
		itemDescriptionLabel.positioning(Positioning.absolute(28, 5));
		itemDescriptionLabel.sizing(Sizing.fixed(70), Sizing.fixed(12));
		this.child(itemDescriptionLabel);

		var priceDescriptionLabel = Components.label(this.priceDescription).horizontalTextAlignment(HorizontalAlignment.RIGHT);
		priceDescriptionLabel.positioning(Positioning.absolute(95, 5));
		priceDescriptionLabel.sizing(Sizing.fixed(55), Sizing.fixed(12));
		this.child(priceDescriptionLabel);

		var playerHeadComponent = Components.texture(this.profileTexture);
		playerHeadComponent.positioning(Positioning.absolute(153, 5));
		playerHeadComponent.sizing(Sizing.fixed(8), Sizing.fixed(8));
		playerHeadComponent.tooltip(this.sellerTooltip);
		this.child(playerHeadComponent);

		var tooltipOverlay = Components.box(Sizing.fixed(126), Sizing.fixed(18));
		tooltipOverlay.color(Color.ofArgb(0x00000000));
		tooltipOverlay.positioning(Positioning.absolute(26, 0));
		tooltipOverlay.tooltip(this.offerTooltip);
		this.child(tooltipOverlay);
	}

	@Override
	public boolean onMouseDown(double mouseX, double mouseY, int button) {
		this.playInteractionSound();
		this.onPress.accept(this);
		return super.onMouseDown(mouseX, mouseY, button);
	}

	private void playInteractionSound() {
		var client = MinecraftClient.getInstance();
		var soundManager = client.getSoundManager();
		var soundInstance = PositionedSoundInstance.master(ModSounds.OFFER_SELECT_SOUND, 0.85F, 1.0F);

		soundManager.play(soundInstance);
	}

}
