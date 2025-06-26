package net.saint.commercialize.screen.market;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.util.LocalizationUtil;
import net.saint.commercialize.util.NumericFormattingUtil;

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

	// Root

	@Override
	protected void build(FlowLayout rootComponent) {
		var wrapperComponent = Containers.verticalFlow(Sizing.fixed(392), Sizing.fixed(192));

		var leftSideComponent = makeLeftSideComponent();
		leftSideComponent.positioning(Positioning.absolute(0, 0));
		leftSideComponent.zIndex(2);

		var rightSideComponent = makeRightSideComponent();
		rightSideComponent.positioning(Positioning.absolute(199, 7));
		rightSideComponent.zIndex(1);

		wrapperComponent.child(leftSideComponent);
		wrapperComponent.child(rightSideComponent);

		rootComponent.child(wrapperComponent);
		rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
		rootComponent.id("market_screen");
	}

	// Left Side

	private Component makeLeftSideComponent() {
		var leftSideComponent = Containers.verticalFlow(Sizing.fixed(202), Sizing.fixed(192));

		var backgroundComponent = Components.texture(MarketScreenAssets.LEFT_PANEL_TEXTURE, 0, 0, 202, 192);
		backgroundComponent.positioning(Positioning.absolute(0, 0));
		leftSideComponent.child(backgroundComponent);

		var offersLabel = Components.label(LocalizationUtil.localizedText("gui", "market.offers"));
		offersLabel.positioning(Positioning.absolute(32, 7));
		offersLabel.color(Color.WHITE);
		leftSideComponent.child(offersLabel);

		var offersSearchBox = makeSearchBoxComponent();
		offersSearchBox.positioning(Positioning.absolute(32, 17));
		leftSideComponent.child(offersSearchBox);

		// Tabs

		var sortingTabButton = makeTabButtonComponent(MarketScreenAssets.SORT_BY_NAME_ICON, component -> {
		});

		sortingTabButton.positioning(Positioning.absolute(4, 21));
		leftSideComponent.child(sortingTabButton);

		var filteringTabButton = makeTabButtonComponent(MarketScreenAssets.FILTER_BY_ALL_ICON, component -> {
		});

		filteringTabButton.positioning(Positioning.absolute(4, 50));
		leftSideComponent.child(filteringTabButton);

		return leftSideComponent;
	}

	// Right Side

	private Component makeRightSideComponent() {
		var rightSideComponent = Containers.verticalFlow(Sizing.fixed(192), Sizing.fixed(178));

		var backgroundComponent = Components.texture(MarketScreenAssets.RIGHT_PANEL_TEXTURE, 0, 0, 192, 178);
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

		var emptyCardTabButton = makeTabButtonComponent(MarketScreenAssets.EMPTY_CART_ICON, component -> {
		});

		emptyCardTabButton.positioning(Positioning.absolute(168, 14));
		rightSideComponent.child(emptyCardTabButton);

		var orderTabButton = makeTabButtonComponent(MarketScreenAssets.CONFIRM_ORDER_ICON, component -> {
		});

		orderTabButton.positioning(Positioning.absolute(168, 43));
		rightSideComponent.child(orderTabButton);

		var cyclePaymentMethodTabButton = makeTabButtonComponent(MarketScreenAssets.WALLET_ICON, component -> {
		});

		cyclePaymentMethodTabButton.positioning(Positioning.absolute(168, 72));
		rightSideComponent.child(cyclePaymentMethodTabButton);

		return rightSideComponent;
	}

	// Components

	private Component makeTotalDisplayComponent() {
		var totalDisplay = Components.label(Text.of(NumericFormattingUtil.formatCurrency(0)));

		totalDisplay.color(Color.WHITE);
		totalDisplay.sizing(Sizing.fixed(105), Sizing.fixed(11));
		totalDisplay.horizontalTextAlignment(HorizontalAlignment.RIGHT);
		totalDisplay.id("total");

		return totalDisplay;
	}

	private Component makeBalanceDisplayComponent() {
		var balanceDisplay = Components.label(Text.of(NumericFormattingUtil.formatCurrency(780_064)));

		balanceDisplay.color(Color.WHITE);
		balanceDisplay.sizing(Sizing.fixed(105), Sizing.fixed(11));
		balanceDisplay.horizontalTextAlignment(HorizontalAlignment.RIGHT);
		balanceDisplay.id("balance");

		return balanceDisplay;
	}

	private Component makeTotalLabelComponent() {
		var totalLabel = Components.label(LocalizationUtil.localizedText("gui", "market.total"));

		totalLabel.color(Color.ofRgb(0x3F3F3F));
		totalLabel.sizing(Sizing.fixed(32), Sizing.fixed(12));

		return totalLabel;
	}

	private Component makeBalanceLabelComponent() {
		var balanceLabel = Components.label(LocalizationUtil.localizedText("gui", "market.cash"));

		balanceLabel.color(Color.ofRgb(0x3F3F3F));
		balanceLabel.sizing(Sizing.fixed(32), Sizing.fixed(12));

		return balanceLabel;
	}

	private Component makeSearchBoxComponent() {
		var offersSearchBox = Components.textBox(Sizing.fixed(162));

		offersSearchBox.cursorStyle(CursorStyle.NONE);
		offersSearchBox.sizing(Sizing.fixed(161), Sizing.fixed(14));
		offersSearchBox.setPlaceholder(LocalizationUtil.localizedText("gui", "market.search_offers"));
		offersSearchBox.setTooltip(Tooltip.of(LocalizationUtil.localizedText("gui", "market.search_offers.tooltip")));

		return offersSearchBox;
	}

	private Component makeTabButtonComponent(TextureReference texture, Consumer<ButtonComponent> onPress) {
		var tabButton = Components.button(Text.empty(), onPress);

		tabButton.sizing(Sizing.fixed(20));
		tabButton.renderer((context, button, delta) -> {
			ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
			texture.draw(context, button.x() + 2, button.y() + 2);
		});

		return tabButton;
	}

}
