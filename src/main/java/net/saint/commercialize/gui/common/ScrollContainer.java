package net.saint.commercialize.gui.common;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;

public class ScrollContainer<C extends Component> extends io.wispforest.owo.ui.container.ScrollContainer<C> {

	// Properties

	protected double lastScrollPosition = 0;

	// Init

	public ScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
		super(direction, horizontalSizing, verticalSizing, child);

		this.scrollbar = Scrollbar.vanilla();
		this.scrollbarThiccness = 8;
	}

	// Logic

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		updateScrollbarInteraction();
		super.draw(context, mouseX, mouseY, partialTicks, delta);
	}

	private void updateScrollbarInteraction() {
		this.lastScrollbarInteractTime = System.currentTimeMillis() + 10_000;
	}

	public void scrollToTop() {
		this.scrollOffset = 0;
	}

	public void markScrollPositionForRestore() {
		lastScrollPosition = currentScrollPosition;
	}

	public void restoreScrollPosition() {
		this.scrollOffset = lastScrollPosition;
	}

}
