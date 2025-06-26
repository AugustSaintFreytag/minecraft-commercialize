package net.saint.commercialize.gui.common;

import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;

public class TextBoxComponent extends io.wispforest.owo.ui.component.TextBoxComponent {

	public TextBoxComponent(Sizing horizontalSizing) {
		super(horizontalSizing);
	}

	protected CursorStyle owo$preferredCursorStyle() {
		return CursorStyle.NONE;
	}

}
