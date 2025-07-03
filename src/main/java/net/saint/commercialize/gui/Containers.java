package net.saint.commercialize.gui;

import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.RenderEffectWrapper;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import net.saint.commercialize.gui.common.ScrollContainer;

public final class Containers {

	// Layout

	public static GridLayout grid(Sizing horizontalSizing, Sizing verticalSizing, int rows, int columns) {
		return io.wispforest.owo.ui.container.Containers.grid(horizontalSizing, verticalSizing, rows, columns);
	}

	public static FlowLayout verticalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
		return io.wispforest.owo.ui.container.Containers.verticalFlow(horizontalSizing, verticalSizing);
	}

	public static FlowLayout horizontalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
		return io.wispforest.owo.ui.container.Containers.horizontalFlow(horizontalSizing, verticalSizing);
	}

	public static FlowLayout ltrTextFlow(Sizing horizontalSizing, Sizing verticalSizing) {
		return io.wispforest.owo.ui.container.Containers.ltrTextFlow(horizontalSizing, verticalSizing);
	}

	public static StackLayout stack(Sizing horizontalSizing, Sizing verticalSizing) {
		return io.wispforest.owo.ui.container.Containers.stack(horizontalSizing, verticalSizing);
	}

	// Scroll

	public static <C extends Component> ScrollContainer<C> verticalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
		return new ScrollContainer<>(ScrollContainer.ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child);
	}

	public static <C extends Component> ScrollContainer<C> horizontalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
		return new ScrollContainer<>(ScrollContainer.ScrollDirection.HORIZONTAL, horizontalSizing, verticalSizing, child);
	}

	// Utility

	public static <C extends Component> DraggableContainer<C> draggable(Sizing horizontalSizing, Sizing verticalSizing, C child) {
		return io.wispforest.owo.ui.container.Containers.draggable(horizontalSizing, verticalSizing, child);
	}

	public static CollapsibleContainer collapsible(Sizing horizontalSizing, Sizing verticalSizing, Text title, boolean expanded) {
		return io.wispforest.owo.ui.container.Containers.collapsible(horizontalSizing, verticalSizing, title, expanded);
	}

	public static <C extends Component> OverlayContainer<C> overlay(C child) {
		return io.wispforest.owo.ui.container.Containers.overlay(child);
	}

	public static <C extends Component> RenderEffectWrapper<C> renderEffect(C child) {
		return io.wispforest.owo.ui.container.Containers.renderEffect(child);
	}

}
