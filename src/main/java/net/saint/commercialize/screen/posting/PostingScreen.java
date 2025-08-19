package net.saint.commercialize.screen.posting;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
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
import net.saint.commercialize.data.text.TimePreset;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.Containers;
import net.saint.commercialize.gui.common.CurrencyTextBoxComponent;
import net.saint.commercialize.gui.common.SelectDropdownComponent;
import net.saint.commercialize.gui.common.TabButtonComponent;
import net.saint.commercialize.gui.slot.CustomSlot;
import net.saint.commercialize.screen.icons.ScreenAssets;
import net.saint.commercialize.util.LocalizationUtil;

public class PostingScreen extends BaseOwoHandledScreen<FlowLayout, PostingScreenHandler> {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_screen");

	private static final int NUMBER_OF_SLOTS = 9 * 4 + 1;

	public PostingScreenDelegate delegate;

	// Init

	public PostingScreen(PostingScreenHandler handler, PlayerInventory playerInventory, Text title) {
		super(handler, playerInventory, title);
	}

	// References

	private OverlayContainer<FlowLayout> overlayComponent;

	// Internals

	@Override
	protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	public boolean shouldPause() {
		return false;
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

		updateDisplay();
	}

	@Override
	protected void build(FlowLayout rootComponent) {
		var wrapperComponent = Containers.verticalFlow(Sizing.fixed(215), Sizing.fixed(215));

		var backgroundComponent = Components.texture(PostingScreenAssets.PANEL);
		backgroundComponent.sizing(Sizing.fixed(215), Sizing.fixed(215));
		backgroundComponent.positioning(Positioning.absolute(0, 0));
		wrapperComponent.child(backgroundComponent);

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

		var priceTextBox = Components.currencyTextBox(Sizing.fixed(101));
		priceTextBox.id("price_input");
		priceTextBox.positioning(Positioning.absolute(74, 64));
		priceTextBox.sizing(Sizing.fixed(101), Sizing.fixed(14));
		priceTextBox.setEditableColor(0xfcfcfc);
		priceTextBox.setUneditableColor(0xfcfcfc);

		priceTextBox.onValueChanged().subscribe(value -> {
			this.delegate.updateOfferPrice(value);
		});

		wrapperComponent.child(priceTextBox);

		// Duration Row

		var durationLabel = Components.label(LocalizationUtil.localizedText("gui", "selling.duration"));
		durationLabel.positioning(Positioning.absolute(13, 86));
		durationLabel.sizing(Sizing.fixed(58), Sizing.fixed(8));
		durationLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(durationLabel);

		var durationDropdown = Components.selectDropdown(PostingScreenUtil.offerDurationDropdownOptions());
		durationDropdown.id("offer_time_dropdown");
		durationDropdown.positioning(Positioning.absolute(74, 83));
		durationDropdown.sizing(Sizing.fixed(103), Sizing.fixed(15));
		durationDropdown.popoverWidth(75);
		durationDropdown.value(TimePreset.THREE_DAYS);

		durationDropdown.onChanged().subscribe(value -> {
			this.delegate.updateOfferDuration(value);
		});

		durationDropdown.onOpenOverlay(() -> {
			return openOverlay();
		});

		durationDropdown.onCloseOverlay(() -> {
			closeOverlay();
		});

		wrapperComponent.child(durationDropdown);

		// Post As Row

		var postAsLabel = Components.label(LocalizationUtil.localizedText("gui", "selling.post_as"));
		postAsLabel.positioning(Positioning.absolute(13, 104));
		postAsLabel.sizing(Sizing.fixed(58), Sizing.fixed(8));
		postAsLabel.color(Color.ofRgb(0x3F3F3F));
		wrapperComponent.child(postAsLabel);

		var postAsDropdown = Components.selectDropdown(PostingScreenUtil.offerPostAsDropdownOptions());
		postAsDropdown.id("offer_post_as_dropdown");
		postAsDropdown.positioning(Positioning.absolute(74, 101));
		postAsDropdown.sizing(Sizing.fixed(103), Sizing.fixed(15));
		postAsDropdown.popoverWidth(95);
		postAsDropdown.value(OfferPostStrategy.AS_STACK);

		postAsDropdown.onChanged().subscribe(value -> {
			this.delegate.updatePostStrategy(value);
		});

		postAsDropdown.onOpenOverlay(() -> {
			return openOverlay();
		});

		postAsDropdown.onCloseOverlay(() -> {
			closeOverlay();
		});

		wrapperComponent.child(postAsDropdown);

		// Tab Buttons

		var postOfferTabButton = new TabButtonComponent(LocalizationUtil.localizedText("gui", "selling.post_offer"),
				ScreenAssets.CONFIRM_ORDER_ICON, button -> {
				});
		postOfferTabButton.positioning(Positioning.absolute(191, 39));
		postOfferTabButton.tooltip(LocalizationUtil.localizedText("gui", "selling.post_offer.tooltip"));
		wrapperComponent.child(postOfferTabButton);

		var resetPriceTabButton = new TabButtonComponent(LocalizationUtil.localizedText("gui", "selling.reset_price"),
				ScreenAssets.RESET_PRICE_ICON, button -> {
				});
		resetPriceTabButton.positioning(Positioning.absolute(191, 68));
		resetPriceTabButton.tooltip(LocalizationUtil.localizedText("gui", "selling.reset_price.tooltip"));
		wrapperComponent.child(resetPriceTabButton);

		var clearOfferTabButton = new TabButtonComponent(LocalizationUtil.localizedText("gui", "selling.clear_offer"),
				ScreenAssets.EMPTY_CART_ICON, button -> {
				});
		clearOfferTabButton.positioning(Positioning.absolute(191, 97));
		clearOfferTabButton.tooltip(LocalizationUtil.localizedText("gui", "selling.clear_offer.tooltip"));
		wrapperComponent.child(clearOfferTabButton);

		// Finalize

		rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
		rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		rootComponent.child(wrapperComponent);
	}

	// Update

	@SuppressWarnings("unchecked")
	public void updateDisplay() {
		var rootComponent = this.uiAdapter.rootComponent;

		if (delegate == null) {
			// If delegate is not set, data can not be managed and display cannot be updated.
			return;
		}

		var itemStack = delegate.getItemStack();
		var itemDescription = PostingScreenUtil.descriptionForItemStack(itemStack);
		var itemNameDisplay = rootComponent.childById(LabelComponent.class, "item_name_display");
		itemNameDisplay.text(itemDescription);

		var itemOfferPrice = delegate.getOfferPrice();
		var priceInput = rootComponent.childById(CurrencyTextBoxComponent.class, "price_input");
		priceInput.value(itemOfferPrice);

		var itemOfferDuration = delegate.getOfferDuration();
		var durationDropdown = (SelectDropdownComponent<Long>) rootComponent.childById(SelectDropdownComponent.class,
				"offer_time_dropdown");
		durationDropdown.value(itemOfferDuration);

		var itemPostStrategy = delegate.getPostStrategy();
		var postStrategyDropdown = (SelectDropdownComponent<OfferPostStrategy>) rootComponent.childById(SelectDropdownComponent.class,
				"offer_post_as_dropdown");
		postStrategyDropdown.value(itemPostStrategy);
	}

	// Overlay

	private FlowLayout openOverlay() {
		if (this.overlayComponent != null) {
			// Re-create overlay even if it's already set up for convenience.
			this.overlayComponent.remove();
		}

		var overlayWrapperComponent = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
		overlayWrapperComponent.id("overlay_wrapper");

		var overlayComponent = Containers.overlay(overlayWrapperComponent);
		overlayComponent.id("overlay");
		overlayComponent.surface(Surface.BLANK);

		uiAdapter.rootComponent.child(overlayComponent);
		this.overlayComponent = overlayComponent;

		disableSlots();

		return overlayWrapperComponent;
	}

	private void closeOverlay() {
		if (this.overlayComponent != null) {
			this.overlayComponent.remove();
			this.overlayComponent = null;
		}

		enableSlots();
	}

	private void disableSlots() {
		for (var index = 0; index < NUMBER_OF_SLOTS; index++) {
			var slot = (CustomSlot) this.handler.getSlot(index);
			slot.setInteractible(false);
		}
	}

	private void enableSlots() {
		for (var index = 0; index < NUMBER_OF_SLOTS; index++) {
			var slot = (CustomSlot) this.handler.getSlot(index);
			slot.setInteractible(true);
		}
	}

}
