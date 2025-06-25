package net.saint.commercialize.screen;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.util.LocalizationUtil;

public class MarketScreen extends BaseOwoScreen<FlowLayout> {

	// Library

	private static class TextureReference {
		public final Identifier texture;
		public final int u;
		public final int v;
		public final int width;
		public final int height;

		public TextureReference(Identifier texture, int u, int v, int width, int height) {
			this.texture = texture;
			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
		}

		public void draw(DrawContext context, int x, int y) {
			context.drawTexture(texture, x, y, u, v, width, height);
		}
	}

	// Configuration

	public static final String ID = "market_screen";

	private static final Identifier LEFT_PANEL_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_left_panel.png");
	private static final Identifier RIGHT_PANEL_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_right_panel.png");
	private static final Identifier ICON_TEXTURE = new Identifier(Commercialize.MOD_ID, "textures/gui/market_block_icons.png");

	private static final TextureReference SORT_BY_NAME_ICON = new TextureReference(ICON_TEXTURE, 64, 16, 16, 16);
	private static final TextureReference SORT_BY_TIME_ICON = new TextureReference(ICON_TEXTURE, 48, 16, 16, 16);
	private static final TextureReference SORT_BY_PRICE_ICON = new TextureReference(ICON_TEXTURE, 32, 16, 16, 16);
	private static final TextureReference SORT_BY_PLAYER_ICON = new TextureReference(ICON_TEXTURE, 16, 16, 16, 16);
	private static final TextureReference FILTER_BY_PRICE_ICON = new TextureReference(ICON_TEXTURE, 16, 32, 16, 16);
	private static final TextureReference FILTER_BY_ALL_ICON = new TextureReference(ICON_TEXTURE, 32, 32, 16, 16);

	private static final TextureReference EMPTY_CART_ICON = new TextureReference(ICON_TEXTURE, 96, 16, 16, 16);
	private static final TextureReference CONFIRM_ORDER_ICON = new TextureReference(ICON_TEXTURE, 112, 16, 16, 16);
	private static final TextureReference WALLET_ICON = new TextureReference(ICON_TEXTURE, 96, 32, 16, 16);
	private static final TextureReference CARD_ICON = new TextureReference(ICON_TEXTURE, 112, 32, 16, 16);

	private static final TextureReference SORT_ASCENDING_ICON = new TextureReference(ICON_TEXTURE, 16, 0, 16, 16);
	private static final TextureReference SORT_DESCENDING_ICON = new TextureReference(ICON_TEXTURE, 32, 0, 16, 16);

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

		var backgroundComponent = Components.texture(LEFT_PANEL_TEXTURE, 0, 0, 202, 192);
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

		var sortingTabButton = makeTabButtonComponent(SORT_BY_NAME_ICON, component -> {
		});

		sortingTabButton.positioning(Positioning.absolute(4, 21));
		leftSideComponent.child(sortingTabButton);

		var filteringTabButton = makeTabButtonComponent(FILTER_BY_ALL_ICON, component -> {
		});

		filteringTabButton.positioning(Positioning.absolute(4, 50));
		leftSideComponent.child(filteringTabButton);

		return leftSideComponent;
	}

	// Right Side

	private Component makeRightSideComponent() {
		var rightSideComponent = Containers.verticalFlow(Sizing.fixed(192), Sizing.fixed(178));

		var backgroundComponent = Components.texture(RIGHT_PANEL_TEXTURE, 0, 0, 192, 178);
		backgroundComponent.positioning(Positioning.absolute(0, 0));
		rightSideComponent.child(backgroundComponent);

		// Tabs

		var emptyCardTabButton = makeTabButtonComponent(EMPTY_CART_ICON, component -> {
		});

		emptyCardTabButton.positioning(Positioning.absolute(168, 14));
		rightSideComponent.child(emptyCardTabButton);

		var orderTabButton = makeTabButtonComponent(CONFIRM_ORDER_ICON, component -> {
		});

		orderTabButton.positioning(Positioning.absolute(168, 43));
		rightSideComponent.child(orderTabButton);

		var cyclePaymentMethodTabButton = makeTabButtonComponent(WALLET_ICON, component -> {
		});

		cyclePaymentMethodTabButton.positioning(Positioning.absolute(168, 72));
		rightSideComponent.child(cyclePaymentMethodTabButton);

		return rightSideComponent;
	}

	// Components

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
