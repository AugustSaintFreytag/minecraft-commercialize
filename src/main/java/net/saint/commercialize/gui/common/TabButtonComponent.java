package net.saint.commercialize.gui.common;

import java.util.function.Consumer;

import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.saint.commercialize.data.offer.OfferSortOrder;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.screen.market.MarketScreenAssets;

public class TabButtonComponent extends net.saint.commercialize.gui.common.ButtonComponent {

	// Properties

	protected Text narrationMessage;
	protected TextureReference texture;
	protected OfferSortOrder sortOrder;

	// Accessors

	public TextureReference texture() {
		return this.texture;
	}

	public void texture(TextureReference texture) {
		this.texture = texture;
	}

	public OfferSortOrder sortOrder() {
		return this.sortOrder;
	}

	public void sortOrder(OfferSortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	protected MutableText getNarrationMessage() {
		return this.narrationMessage.copy();
	}

	// Init

	public TabButtonComponent(Text message, TextureReference texture, Consumer<TabButtonComponent> onPress) {
		super(Text.empty(), component -> {
			onPress.accept((TabButtonComponent) component);
		});

		this.narrationMessage = message;
		this.texture = texture;
		this.sizing(Sizing.fixed(20));

		this.renderer((context, button, delta) -> {
			ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
			this.texture.draw(context, button.x() + 2, button.y() + 2);
			overlayTextureForSortOrder(sortOrder).draw(context, button.x() + 1, button.y() + 2);
		});
	}

	private static TextureReference overlayTextureForSortOrder(OfferSortOrder sortOrder) {
		if (sortOrder == null) {
			return MarketScreenAssets.STUB_ICON;
		}

		return switch (sortOrder) {
		case ASCENDING -> MarketScreenAssets.SORT_ASCENDING_ICON;
		case DESCENDING -> MarketScreenAssets.SORT_DESCENDING_ICON;
		};
	}

}
