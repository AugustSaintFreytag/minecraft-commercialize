package net.saint.commercialize.gui;

import java.util.function.Consumer;

import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.saint.commercialize.gui.common.ButtonComponent;
import net.saint.commercialize.gui.common.TextBoxComponent;
import net.saint.commercialize.library.TextureReference;

public final class Components {

	public static TextureComponent texture(Identifier texture, int u, int v, int regionWidth, int regionHeight) {
		return io.wispforest.owo.ui.component.Components.texture(texture, u, v, regionWidth, regionHeight);
	}

	public static TextureComponent texture(TextureReference texture) {
		return io.wispforest.owo.ui.component.Components.texture(texture.texture, texture.u, texture.v, texture.width, texture.height);
	}

	public static LabelComponent label(Text text) {
		return io.wispforest.owo.ui.component.Components.label(text);
	}

	public static TextBoxComponent textBox(Sizing sizing) {
		return new TextBoxComponent(sizing);
	}

	public static ButtonComponent button(Text message, Consumer<ButtonComponent> onPress) {
		return new ButtonComponent(message, onPress);
	}

	public static ItemComponent item(ItemStack item) {
		return io.wispforest.owo.ui.component.Components.item(item);
	}

}
