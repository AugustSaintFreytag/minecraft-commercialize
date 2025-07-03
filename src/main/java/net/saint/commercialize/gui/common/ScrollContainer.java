package net.saint.commercialize.gui.common;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;

public class ScrollContainer<C extends Component> extends io.wispforest.owo.ui.container.ScrollContainer<C> {

	public ScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
		super(direction, horizontalSizing, verticalSizing, child);

		this.scrollbar = Scrollbar.vanilla();
		this.scrollbarThiccness = 8;
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		updateScrollbarInteraction();
		super.draw(context, mouseX, mouseY, partialTicks, delta);
	}

	private void updateScrollbarInteraction() {
		this.lastScrollbarInteractTime = System.currentTimeMillis() + 10_000;
	}

}
