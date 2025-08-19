package net.saint.commercialize.screen.market;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.item.ItemNameFormattingUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.player.PlayerHeadUtil;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.Containers;
import net.saint.commercialize.gui.common.ScrollContainer;
import net.saint.commercialize.gui.common.TabButtonComponent;
import net.saint.commercialize.gui.common.TextBoxComponent;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.screen.icons.ScreenAssets;
import net.saint.commercialize.screen.market.components.CartListComponent;
import net.saint.commercialize.screen.market.components.CurrencyDisplayComponent;
import net.saint.commercialize.screen.market.components.OfferListCapComponent;
import net.saint.commercialize.screen.market.components.OfferListComponent;
import net.saint.commercialize.util.LocalizationUtil;

public class MarketScreen extends BaseOwoScreen<FlowLayout> {

	// Configuration

	public static final String ID = "market_screen";

	// References

	public MarketScreenDelegate delegate;

	// Init

	@Override
	protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		delegate.onScreenClose();
		super.close();
	}

	// Update

	public void updateDisplay() {
		if (delegate == null) {
			Commercialize.LOGGER.warn("Can not update market screen display, missing delegate.");
			return;
		}

		var rootComponent = this.uiAdapter.rootComponent;

		// Inputs & Controls

		var searchTextBox = rootComponent.childById(TextBoxComponent.class, "search_input");
		if (searchTextBox.getText() != delegate.getSearchTerm()) {
			searchTextBox.text(delegate.getSearchTerm());
		}

		var sortModeButton = rootComponent.childById(TabButtonComponent.class, "sort_mode");
		sortModeButton.sortOrder(delegate.getSortOrder());
		sortModeButton.texture(MarketScreenUtil.textureForSortMode(delegate.getSortMode()));
		sortModeButton.tooltip(MarketScreenUtil.tooltipTextForSortMode(delegate.getSortMode(), delegate.getSortOrder()));

		var filterModeButton = rootComponent.childById(TabButtonComponent.class, "filter_mode");
		filterModeButton.texture(MarketScreenUtil.textureForFilterMode(delegate.getFilterMode()));
		filterModeButton.tooltip(MarketScreenUtil.tooltipTextForFilterMode(delegate.getFilterMode()));

		var paymentMethodButton = rootComponent.childById(TabButtonComponent.class, "payment_method");
		paymentMethodButton.texture(MarketScreenUtil.textureForPaymentMethod(delegate.getPaymentMethod()));
		paymentMethodButton.tooltip(MarketScreenUtil.tooltipTextForPaymentMethod(delegate.getPaymentMethod(), delegate.getCardOwnerName()));

		var totalDisplay = rootComponent.childById(CurrencyDisplayComponent.class, "total");
		var totalDisplayAppearance = MarketScreenUtil.appearanceForCartTotal(delegate.getCartTotal(), delegate.getBalance());
		totalDisplay.text(MarketScreenUtil.textForCartTotal(delegate.getCartTotal()));
		totalDisplay.appearance(totalDisplayAppearance);

		var balanceLabel = rootComponent.childById(LabelComponent.class, "balance_label");
		balanceLabel.text(MarketScreenUtil.labelTextForBalance(delegate.getPaymentMethod()));

		var balanceDisplay = rootComponent.childById(CurrencyDisplayComponent.class, "balance");
		var balance = delegate.getBalance();
		balanceDisplay.text(MarketScreenUtil.textForBalance(balance));
		balanceDisplay.tooltip(MarketScreenUtil.tooltipTextForBalance(delegate.getPaymentMethod(), delegate.getCardOwnerName()));

		if (!Commercialize.CONFIG.allowForeignCardsForMarketPayment && delegate.hasCardInHand() && !delegate.hasOwnedCardInHand()) {
			var balanceText = LocalizationUtil.localizedText("gui", "market.inviable_balance");
			balanceDisplay.text(balanceText);

			var tooltipText = LocalizationUtil.localizedText("gui", "market.inviable_account.tooltip", delegate.getCardOwnerName());
			balanceDisplay.tooltip(tooltipText);
		}

		// Cart

		if (delegate.getCart().isEmpty()) {
			addEmptyCartIndicator(rootComponent);
		} else {
			removeEmptyCartIndicator(rootComponent);
		}

		var cartContainer = rootComponent.childById(FlowLayout.class, "cart_container");
		cartContainer.clearChildren();

		var cartOffers = delegate.getCart();
		cartOffers.forEach(offer -> {
			var cartComponent = makeCartListComponent(offer);
			cartContainer.child(cartComponent);
		});

		// Offers

		var offerScrollView = rootComponent.childById(ScrollContainer.class, "offer_scroll_view");
		offerScrollView.markScrollPositionForRestore();

		var offerContainer = rootComponent.childById(FlowLayout.class, "offer_container");
		offerContainer.clearChildren();

		var offers = delegate.getOffers();
		var offersAreCapped = delegate.getOffersAreCapped();
		var numberOfOffers = offers.size();

		Commercialize.LOGGER.info("Rendering {} offer(s) in market screen with cap: {}.", numberOfOffers, offersAreCapped);

		offers.forEach(offer -> {
			var offerIsInCart = delegate.hasOfferInCart(offer);
			var offerComponent = makeOfferListComponent(offer, offerIsInCart);
			offerContainer.child(offerComponent);
		});

		if (offersAreCapped) {
			var offerCapComponent = makeOfferListCapComponent();
			offerContainer.child(offerCapComponent);
		}

		offerScrollView.restoreScrollPosition();
	}

	public void resetOfferScrollView() {
		var rootComponent = this.uiAdapter.rootComponent;
		var offerScrollView = rootComponent.childById(ScrollContainer.class, "offer_scroll_view");
		offerScrollView.scrollToTop();
	}

	// Root

	@Override
	protected void build(FlowLayout rootComponent) {
		var wrapperComponent = Containers.verticalFlow(Sizing.fixed(404), Sizing.fixed(192));

		var leftSideComponent = makeLeftSideComponent();
		leftSideComponent.positioning(Positioning.absolute(0, 0));
		leftSideComponent.zIndex(2);

		var rightSideComponent = makeRightSideComponent();
		rightSideComponent.positioning(Positioning.absolute(211, 7));
		rightSideComponent.zIndex(1);

		wrapperComponent.child(leftSideComponent);
		wrapperComponent.child(rightSideComponent);

		rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
		rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		rootComponent.child(wrapperComponent);

		updateDisplay();
	}

	// Left Side

	private FlowLayout makeLeftSideComponent() {
		var leftSideComponent = Containers.verticalFlow(Sizing.fixed(214), Sizing.fixed(192));

		// Background

		var backgroundComponent = Components.texture(MarketScreenAssets.LEFT_PANEL);
		backgroundComponent.positioning(Positioning.absolute(0, 0));
		leftSideComponent.child(backgroundComponent);

		// Labels

		var offersLabel = Components.label(LocalizationUtil.localizedText("gui", "market.offers"));
		offersLabel.positioning(Positioning.absolute(32, 7));
		offersLabel.color(Color.WHITE);
		leftSideComponent.child(offersLabel);

		var offersSearchBox = makeSearchBoxComponent();
		offersSearchBox.positioning(Positioning.absolute(32, 17));
		offersSearchBox.onChanged().subscribe(updatedSearchTerm -> {
			// Update search term and trigger update.
			delegate.setSearchTerm(updatedSearchTerm);
			this.updateDisplay();
		});

		leftSideComponent.child(offersSearchBox);

		// Offers

		var offerContainer = Containers.verticalFlow(Sizing.fixed(166), Sizing.content()).id("offer_container");
		offerContainer.positioning(Positioning.absolute(0, 0));

		var offerScrollView = Containers.verticalScroll(Sizing.fixed(174), Sizing.fixed(148), offerContainer).id("offer_scroll_view");
		offerScrollView.positioning(Positioning.absolute(33, 36));

		leftSideComponent.child(offerScrollView);

		// Tabs

		var sortingTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.sort_mode"), ScreenAssets.STUB,
				component -> {
					// If sprint key is held, toggle sort order.

					var windowHandle = client.getWindow().getHandle();
					var sprintKeyCode = KeyBindingHelper.getBoundKeyOf(client.options.sprintKey).getCode();
					var isSprintKeyHeld = InputUtil.isKeyPressed(windowHandle, sprintKeyCode);

					if (isSprintKeyHeld) {
						delegate.cycleSortOrder();
					} else {
						delegate.cycleSortMode();
					}

					this.updateDisplay();
				});

		sortingTabButton.id("sort_mode");
		sortingTabButton.positioning(Positioning.absolute(4, 21));
		leftSideComponent.child(sortingTabButton);

		var filteringTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.filter_mode"), ScreenAssets.STUB,
				component -> {
					delegate.cycleFilterMode();
					this.updateDisplay();
				});

		filteringTabButton.id("filter_mode");
		filteringTabButton.positioning(Positioning.absolute(4, 50));
		leftSideComponent.child(filteringTabButton);

		return leftSideComponent;
	}

	// Right Side

	private FlowLayout makeRightSideComponent() {
		var rightSideComponent = Containers.verticalFlow(Sizing.fixed(192), Sizing.fixed(178));
		rightSideComponent.id("right_side");

		var backgroundComponent = Components.texture(MarketScreenAssets.RIGHT_PANEL);
		backgroundComponent.positioning(Positioning.absolute(0, 0));
		rightSideComponent.child(backgroundComponent);

		// Cart

		var cartContainer = Containers.verticalFlow(Sizing.fixed(136), Sizing.content()).id("cart_container");
		cartContainer.positioning(Positioning.absolute(0, 0));

		var cartScrollView = Containers.verticalScroll(Sizing.fixed(140), Sizing.fixed(98), cartContainer);
		cartScrollView.positioning(Positioning.absolute(14, 14));

		rightSideComponent.child(cartScrollView);

		// Labels

		var totalLabelComponent = makeTotalLabelComponent();
		totalLabelComponent.positioning(Positioning.absolute(11, 129));
		rightSideComponent.child(totalLabelComponent);

		var balanceLabelComponent = makeBalanceLabelComponent();
		balanceLabelComponent.positioning(Positioning.absolute(11, 157));
		rightSideComponent.child(balanceLabelComponent);

		// Displays

		var totalDisplayComponent = makeTotalDisplayComponent();
		totalDisplayComponent.positioning(Positioning.absolute(46, 127));
		rightSideComponent.child(totalDisplayComponent);

		var balanceDisplayComponent = makeBalanceDisplayComponent();
		balanceDisplayComponent.positioning(Positioning.absolute(46, 155));
		rightSideComponent.child(balanceDisplayComponent);

		// Tabs

		var orderTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.order_cart"),
				ScreenAssets.CONFIRM_ORDER_ICON, component -> {
					if (delegate.getCart().isEmpty()) {
						client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 0.5f);
						return;
					}

					delegate.confirmCartOrder();
				});

		orderTabButton.tooltip(LocalizationUtil.localizedText("gui", "market.order_cart.tooltip"));
		orderTabButton.positioning(Positioning.absolute(168, 14));
		rightSideComponent.child(orderTabButton);

		var cyclePaymentMethodTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.payment_mode"),
				ScreenAssets.STUB, component -> {
					delegate.cyclePaymentMethod();
					this.updateDisplay();
				});

		cyclePaymentMethodTabButton.id("payment_method");
		cyclePaymentMethodTabButton.positioning(Positioning.absolute(168, 43));
		rightSideComponent.child(cyclePaymentMethodTabButton);

		var emptyCardTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.empty_cart"),
				ScreenAssets.EMPTY_CART_ICON, component -> {
					delegate.emptyCart();
				});

		emptyCardTabButton.tooltip(LocalizationUtil.localizedText("gui", "market.empty_cart.tooltip"));
		emptyCardTabButton.positioning(Positioning.absolute(168, 72));
		rightSideComponent.child(emptyCardTabButton);

		return rightSideComponent;
	}

	// Toggleables

	private void addEmptyCartIndicator(FlowLayout rootComponent) {
		if (rootComponent.childById(FlowLayout.class, "empty_cart_indicator") != null) {
			return;
		}

		var emptyCartIndicatorComponent = makeEmptyCartIndicatorComponent();
		emptyCartIndicatorComponent.id("empty_cart_indicator");
		emptyCartIndicatorComponent.positioning(Positioning.absolute(14, 14));
		emptyCartIndicatorComponent.sizing(Sizing.fixed(140), Sizing.fixed(98));

		var rightSideComponent = rootComponent.childById(FlowLayout.class, "right_side");
		rightSideComponent.child(emptyCartIndicatorComponent);
	}

	private void removeEmptyCartIndicator(FlowLayout rootComponent) {
		var emptyCartIndicatorComponent = rootComponent.childById(FlowLayout.class, "empty_cart_indicator");

		if (emptyCartIndicatorComponent == null) {
			return;
		}

		emptyCartIndicatorComponent.remove();
	}

	// Components

	private CurrencyDisplayComponent makeTotalDisplayComponent() {
		var displayComponent = new CurrencyDisplayComponent(Text.empty(), CurrencyDisplayComponent.Appearance.NEUTRAL);
		displayComponent.id("total");

		return displayComponent;
	}

	private CurrencyDisplayComponent makeBalanceDisplayComponent() {
		var displayComponent = new CurrencyDisplayComponent(Text.empty(), CurrencyDisplayComponent.Appearance.NEUTRAL);
		displayComponent.id("balance");

		return displayComponent;
	}

	private LabelComponent makeTotalLabelComponent() {
		var totalLabel = Components.label(LocalizationUtil.localizedText("gui", "market.total"));

		totalLabel.id("total_label");
		totalLabel.color(Color.ofRgb(0x3F3F3F));
		totalLabel.sizing(Sizing.fixed(32), Sizing.fixed(12));

		return totalLabel;
	}

	private LabelComponent makeBalanceLabelComponent() {
		var balanceLabel = Components.label(LocalizationUtil.localizedText("gui", "market.cash"));

		balanceLabel.id("balance_label");
		balanceLabel.color(Color.ofRgb(0x3F3F3F));
		balanceLabel.sizing(Sizing.fixed(32), Sizing.fixed(12));

		return balanceLabel;
	}

	private TextBoxComponent makeSearchBoxComponent() {
		var offersSearchBox = Components.textBox(Sizing.fixed(162));

		offersSearchBox.id("search_input");
		offersSearchBox.sizing(Sizing.fixed(174), Sizing.fixed(14));
		offersSearchBox.setPlaceholder(LocalizationUtil.localizedText("gui", "market.search_offers"));
		offersSearchBox.setTooltip(Tooltip.of(LocalizationUtil.localizedText("gui", "market.search_offers.tooltip")));

		return offersSearchBox;
	}

	private TabButtonComponent makeTabButtonComponent(Text message, TextureReference texture, Consumer<TabButtonComponent> onPress) {
		return new TabButtonComponent(message, texture, onPress);
	}

	private OfferListComponent makeOfferListComponent(Offer offer, boolean isDisabled) {
		var itemStack = offer.stack;
		var itemDescription = ItemNameFormattingUtil.abbreviatedItemText(itemStack, 12);
		var priceDescription = Text.of(CurrencyFormattingUtil.formatCurrency(offer.price));
		var offerTooltip = MarketScreenUtil.tooltipTextForOffer(client.world, offer);
		var sellerTooltip = MarketScreenUtil.tooltipTextForSeller(offer);
		var sellerTexture = profileTextureForOffer(offer);

		return new OfferListComponent(itemStack, itemDescription, priceDescription, offerTooltip, sellerTooltip, sellerTexture, isDisabled,
				component -> {
					if (delegate.hasOfferInCart(offer)) {
						delegate.removeOfferFromCart(offer);
					} else {
						delegate.addOfferToCart(offer);
					}
				});
	}

	private OfferListCapComponent makeOfferListCapComponent() {
		return new OfferListCapComponent();
	}

	private TextureReference profileTextureForOffer(Offer offer) {
		if (offer.isGenerated) {
			var sellerName = offer.sellerName;
			var texture = PlayerHeadUtil.playerHeadTextureForName(sellerName);

			return texture;
		}

		var sellerProfile = new GameProfile(offer.sellerId, offer.sellerName);
		var textureIdentifier = Commercialize.PLAYER_PROFILE_MANAGER.skinForPlayer(sellerProfile);
		var texture = new TextureReference(textureIdentifier, 32, 32, 32, 32);

		return texture;
	}

	private CartListComponent makeCartListComponent(Offer offer) {
		var itemStack = offer.stack;
		var itemDescription = ItemNameFormattingUtil.abbreviatedItemText(itemStack, 9);
		var priceDescription = Text.of(CurrencyFormattingUtil.formatCurrency(offer.price));
		var offerTooltip = MarketScreenUtil.tooltipTextForOffer(client.world, offer);

		return new CartListComponent(itemStack, itemDescription, priceDescription, offerTooltip, component -> {
			this.delegate.removeOfferFromCart(offer);
		});
	}

	private EmptyCartIndicatorComponent makeEmptyCartIndicatorComponent() {
		return new EmptyCartIndicatorComponent();
	}

	// Subcomponents

	private static class EmptyCartIndicatorComponent extends FlowLayout {

		// Init

		public EmptyCartIndicatorComponent() {
			super(Sizing.fill(100), Sizing.content(), FlowLayout.Algorithm.VERTICAL);
			this.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

			var textureComponent = Components.texture(MarketScreenAssets.EMPTY_CART_INDICATOR);
			textureComponent.sizing(Sizing.fixed(48), Sizing.fixed(43));
			this.child(textureComponent);

			var labelComponent = Components.label(LocalizationUtil.localizedText("gui", "market.empty_cart_indicator"));
			labelComponent.horizontalTextAlignment(HorizontalAlignment.CENTER);
			labelComponent.sizing(Sizing.fill(100), Sizing.content());
			labelComponent.margins(Insets.vertical(6));
			labelComponent.color(Color.ofRgb(0x78726D));
			this.child(labelComponent);
		}

	}

}
