package net.saint.commercialize.gui.common;

import java.util.function.Consumer;

import io.wispforest.owo.ui.core.CursorStyle;
import net.minecraft.text.Text;

public class ButtonComponent extends io.wispforest.owo.ui.component.ButtonComponent {

	public ButtonComponent(Text message, Consumer<io.wispforest.owo.ui.component.ButtonComponent> onPress) {
		super(message, onPress);
	}

	@Override
	protected CursorStyle owo$preferredCursorStyle() {
		return CursorStyle.NONE;
	}

}
