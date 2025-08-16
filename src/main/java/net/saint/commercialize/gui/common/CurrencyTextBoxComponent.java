package net.saint.commercialize.gui.common;

import org.lwjgl.glfw.GLFW;

import io.wispforest.owo.ui.core.Sizing;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;

public class CurrencyTextBoxComponent extends TextBoxComponent {

	protected int value;

	// Init

	public CurrencyTextBoxComponent(Sizing horizontalSizing) {
		super(horizontalSizing);

		this.focusGained().subscribe(focusSource -> {
			this.text(String.valueOf(this.value));
		});

		this.focusLost().subscribe(() -> {
			try {
				var newInputValue = this.getText().replaceAll("[.,' ]", "");
				var newValue = Integer.parseInt(newInputValue);

				this.value(newValue);
			} catch (NumberFormatException e) {
				// Discard error, keep original internal value, assume interaction canceled.
			}

			updateDisplayText();
		});

		this.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
				this.root().focusHandler().focus(null, null);
				return true;
			}

			return false;
		});
	}

	// Access

	public int value() {
		return this.value;
	}

	public CurrencyTextBoxComponent value(int value) {
		this.value = value;
		updateDisplayText();

		return this;
	}

	// Lifecycle

	protected void updateDisplayText() {
		this.text(CurrencyFormattingUtil.formatCurrency(this.value));
	}

}
