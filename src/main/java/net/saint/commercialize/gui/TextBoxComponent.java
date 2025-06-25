package net.saint.commercialize.gui;

import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;

public class TextBoxComponent extends io.wispforest.owo.ui.component.TextBoxComponent {

	protected TextBoxComponent(Sizing horizontalSizing) {
		super(horizontalSizing);
	}

	protected CursorStyle owo$preferredCursorStyle() {
		return CursorStyle.NONE;
	}

}
