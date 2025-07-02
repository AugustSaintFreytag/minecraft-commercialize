package net.saint.commercialize.screen.market;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.offer.OfferFilterMode;
import net.saint.commercialize.data.offer.OfferSortMode;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.data.payment.PaymentMethod;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.gui.Containers;
import net.saint.commercialize.gui.assets.MarketAssets;
import net.saint.commercialize.gui.common.TabButtonComponent;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.util.ItemNameAbbreviationUtil;
import net.saint.commercialize.util.LocalizationUtil;
import net.saint.commercialize.util.NumericFormattingUtil;
import net.saint.commercialize.util.PlayerHeadUtil;

public class MarketScreen extends BaseOwoScreen<FlowLayout> {

	// Configuration

	public static final String ID = "market_screen";

	// Init

	@Override
	protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// State

	private OfferSortMode sortMode = OfferSortMode.TIME_POSTED;
	private OfferSortOrder sortOrder = OfferSortOrder.DESCENDING;
	private OfferFilterMode filterMode = OfferFilterMode.ALL;
	private PaymentMethod paymentMethod = PaymentMethod.INVENTORY;

	// Update

	public void updateDisplay() {
		var rootComponent = this.uiAdapter.rootComponent;

		var sortModeButton = rootComponent.childById(TabButtonComponent.class, "sort_mode");
		sortModeButton.sortOrder(sortOrder);
		sortModeButton.texture(MarketScreenUtil.textureForSortMode(sortMode));
		sortModeButton.tooltip(MarketScreenUtil.tooltipTextForSortMode(sortMode, sortOrder));

		var filterModeButton = rootComponent.childById(TabButtonComponent.class, "filter_mode");
		filterModeButton.texture(MarketScreenUtil.textureForFilterMode(filterMode));
		filterModeButton.tooltip(MarketScreenUtil.tooltipTextForFilterMode(filterMode));

		var paymentMethodButton = rootComponent.childById(TabButtonComponent.class, "payment_method");
		paymentMethodButton.texture(MarketScreenUtil.textureForPaymentMethod(paymentMethod));
		paymentMethodButton.tooltip(MarketScreenUtil.tooltipTextForPaymentMethod(paymentMethod));

		var totalDisplay = rootComponent.childById(LabelComponent.class, "total");
		totalDisplay.text(Text.of(NumericFormattingUtil.formatCurrency(0)));

		var balanceLabel = rootComponent.childById(LabelComponent.class, "balance_label");
		balanceLabel.text(MarketScreenUtil.labelTextForBalance(paymentMethod));

		var balanceDisplay = rootComponent.childById(LabelComponent.class, "balance");
		balanceDisplay.text(Text.of(NumericFormattingUtil.formatCurrency(0)));
		balanceDisplay.tooltip(MarketScreenUtil.tooltipTextForBalance(paymentMethod));

		var offerContainer = rootComponent.childById(FlowLayout.class, "offer_container");
		offerContainer.clearChildren();

		Commercialize.LOGGER.info("Rendering {} offer(s) in market screen.", Commercialize.MARKET_MANAGER.size());

		Commercialize.MARKET_MANAGER.getOffers().forEach(offer -> {
			var offerComponent = makeOfferListComponent(offer);
			offerContainer.child(offerComponent);
		});
	}

	// Root

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
	}

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

		rootComponent.child(wrapperComponent);
		rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		rootComponent.id("market_screen");

		updateDisplay();
	}

	// Left Side

	private FlowLayout makeLeftSideComponent() {
		var leftSideComponent = Containers.verticalFlow(Sizing.fixed(214), Sizing.fixed(192));

		var backgroundComponent = Components.texture(MarketAssets.LEFT_PANEL_TEXTURE, 0, 0, 214, 192);
		backgroundComponent.positioning(Positioning.absolute(0, 0));
		leftSideComponent.child(backgroundComponent);

		var offersLabel = Components.label(LocalizationUtil.localizedText("gui", "market.offers"));
		offersLabel.positioning(Positioning.absolute(32, 7));
		offersLabel.color(Color.WHITE);
		leftSideComponent.child(offersLabel);

		var offersSearchBox = makeSearchBoxComponent();
		offersSearchBox.positioning(Positioning.absolute(32, 17));
		leftSideComponent.child(offersSearchBox);

		// Offers

		var offerContainer = Containers.verticalFlow(Sizing.fixed(166), Sizing.content()).id("offer_container");
		offerContainer.positioning(Positioning.absolute(0, 0));

		var offerScrollView = Containers.verticalScroll(Sizing.fixed(174), Sizing.fixed(148), offerContainer);
		offerScrollView.positioning(Positioning.absolute(33, 36));

		leftSideComponent.child(offerScrollView);

		// Tabs

		var sortingTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.sort_mode"),
				MarketScreenUtil.textureForSortMode(sortMode), component -> {
					// If sprint key is held, toggle sort order.

					var windowHandle = client.getWindow().getHandle();
					var sprintKeyCode = KeyBindingHelper.getBoundKeyOf(client.options.sprintKey).getCode();
					var isSprintKeyHeld = InputUtil.isKeyPressed(windowHandle, sprintKeyCode);

					if (isSprintKeyHeld) {
						sortOrder = sortOrder.next();
					} else {
						sortMode = sortMode.next();
					}

					this.updateDisplay();
				});

		sortingTabButton.id("sort_mode");
		sortingTabButton.positioning(Positioning.absolute(4, 21));
		leftSideComponent.child(sortingTabButton);

		var filteringTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.filter_mode"),
				MarketScreenUtil.textureForFilterMode(filterMode), component -> {
					filterMode = filterMode.next();
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

		var backgroundComponent = Components.texture(MarketAssets.RIGHT_PANEL_TEXTURE, 0, 0, 192, 178);
		backgroundComponent.positioning(Positioning.absolute(0, 0));
		rightSideComponent.child(backgroundComponent);

		// Labels

		var totalLabelComponent = makeTotalLabelComponent();
		totalLabelComponent.positioning(Positioning.absolute(11, 129));
		rightSideComponent.child(totalLabelComponent);

		var balanceLabelComponent = makeBalanceLabelComponent();
		balanceLabelComponent.positioning(Positioning.absolute(11, 157));
		rightSideComponent.child(balanceLabelComponent);

		// Displays

		var totalDisplayComponent = makeTotalDisplayComponent();
		totalDisplayComponent.positioning(Positioning.absolute(48, 129));
		rightSideComponent.child(totalDisplayComponent);

		var balanceDisplayComponent = makeBalanceDisplayComponent();
		balanceDisplayComponent.positioning(Positioning.absolute(48, 157));
		rightSideComponent.child(balanceDisplayComponent);

		// Tabs

		var emptyCardTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.empty_cart"),
				MarketAssets.EMPTY_CART_ICON, component -> {
					client.player.sendMessage(Text.of("Requesting to empty cart."));
				});

		emptyCardTabButton.positioning(Positioning.absolute(168, 14));
		emptyCardTabButton.tooltip(LocalizationUtil.localizedText("gui", "market.empty_cart.tooltip"));
		rightSideComponent.child(emptyCardTabButton);

		var orderTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.order_cart"),
				MarketAssets.CONFIRM_ORDER_ICON, component -> {
					client.player.sendMessage(Text.of("Requesting to confirm order."));
				});

		orderTabButton.positioning(Positioning.absolute(168, 43));
		orderTabButton.tooltip(LocalizationUtil.localizedText("gui", "market.order_cart.tooltip"));
		rightSideComponent.child(orderTabButton);

		var cyclePaymentMethodTabButton = makeTabButtonComponent(LocalizationUtil.localizedText("gui", "market.payment_mode"),
				MarketAssets.STUB_ICON, component -> {
					paymentMethod = paymentMethod.next();
					this.updateDisplay();
				});

		cyclePaymentMethodTabButton.id("payment_method");
		cyclePaymentMethodTabButton.positioning(Positioning.absolute(168, 72));
		rightSideComponent.child(cyclePaymentMethodTabButton);

		return rightSideComponent;
	}

	// Components

	private LabelComponent makeTotalDisplayComponent() {
		var totalDisplay = Components.label(Text.empty());

		totalDisplay.id("total");
		totalDisplay.tooltip(LocalizationUtil.localizedText("gui", "market.total.tooltip"));
		totalDisplay.color(Color.WHITE);
		totalDisplay.sizing(Sizing.fixed(105), Sizing.fixed(11));
		totalDisplay.horizontalTextAlignment(HorizontalAlignment.RIGHT);

		return totalDisplay;
	}

	private LabelComponent makeBalanceDisplayComponent() {
		var balanceDisplay = Components.label(Text.empty());

		balanceDisplay.id("balance");
		balanceDisplay.color(Color.WHITE);
		balanceDisplay.sizing(Sizing.fixed(105), Sizing.fixed(11));
		balanceDisplay.horizontalTextAlignment(HorizontalAlignment.RIGHT);

		return balanceDisplay;
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

		offersSearchBox.sizing(Sizing.fixed(174), Sizing.fixed(14));
		offersSearchBox.setPlaceholder(LocalizationUtil.localizedText("gui", "market.search_offers"));
		offersSearchBox.setTooltip(Tooltip.of(LocalizationUtil.localizedText("gui", "market.search_offers.tooltip")));

		return offersSearchBox;
	}

	private TabButtonComponent makeTabButtonComponent(Text message, TextureReference texture, Consumer<TabButtonComponent> onPress) {
		return new TabButtonComponent(message, texture, onPress);
	}

	private OfferListComponent makeOfferListComponent(Offer offer) {
		var itemStack = offer.stack;
		var itemDescription = ItemNameAbbreviationUtil.abbreviatedItemText(itemStack, 12);
		var priceDescription = Text.of(NumericFormattingUtil.formatCurrency(offer.price));
		var offerTooltip = MarketScreenUtil.tooltipTextForOffer(client.world, offer);
		var sellerTooltip = MarketScreenUtil.tooltipTextForSeller(offer);
		var sellerTexture = profileTextureForOffer(offer);

		return new OfferListComponent(itemStack, itemDescription, priceDescription, offerTooltip, sellerTooltip, sellerTexture,
				component -> {
					// Handle offer selection
					client.player.sendMessage(Text.of("Selected offer: " + offer.stack.getName().getString()));
				});
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

	private static class OfferListComponent extends FlowLayout {

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
			this.onPress.accept(this);
			return super.onMouseDown(mouseX, mouseY, button);
		}

	}

}
