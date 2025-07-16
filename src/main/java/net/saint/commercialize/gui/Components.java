package net.saint.commercialize.gui;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.wispforest.owo.ui.component.BlockComponent;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.component.SlimSliderComponent;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.component.SpriteComponent;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.saint.commercialize.gui.common.ButtonComponent;
import net.saint.commercialize.gui.common.TextBoxComponent;
import net.saint.commercialize.gui.common.TextureComponent;
import net.saint.commercialize.library.TextureReference;

public final class Components {

	// Vanilla Components

	public static io.wispforest.owo.ui.component.TextAreaComponent textArea(Sizing horizontalSizing, Sizing verticalSizing) {
		return io.wispforest.owo.ui.component.Components.textArea(horizontalSizing, verticalSizing);
	}

	public static io.wispforest.owo.ui.component.TextAreaComponent textArea(Sizing horizontalSizing, Sizing verticalSizing, String text) {
		return io.wispforest.owo.ui.component.Components.textArea(horizontalSizing, verticalSizing, text);
	}

	// Default Components

	public static <E extends Entity> EntityComponent<E> entity(Sizing sizing, EntityType<E> type, @Nullable NbtCompound nbt) {
		return io.wispforest.owo.ui.component.Components.entity(sizing, type, nbt);
	}

	public static <E extends Entity> EntityComponent<E> entity(Sizing sizing, E entity) {
		return io.wispforest.owo.ui.component.Components.entity(sizing, entity);
	}

	public static ItemComponent item(ItemStack item) {
		return io.wispforest.owo.ui.component.Components.item(item);
	}

	public static BlockComponent block(BlockState state) {
		return io.wispforest.owo.ui.component.Components.block(state);
	}

	public static BlockComponent block(BlockState state, BlockEntity blockEntity) {
		return io.wispforest.owo.ui.component.Components.block(state, blockEntity);
	}

	public static BlockComponent block(BlockState state, @Nullable NbtCompound nbt) {
		return io.wispforest.owo.ui.component.Components.block(state, nbt);
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

	public static CheckboxComponent checkbox(Text message) {
		return io.wispforest.owo.ui.component.Components.checkbox(message);
	}

	public static SliderComponent slider(Sizing horizontalSizing) {
		return io.wispforest.owo.ui.component.Components.slider(horizontalSizing);
	}

	public static DiscreteSliderComponent discreteSlider(Sizing horizontalSizing, double min, double max) {
		return io.wispforest.owo.ui.component.Components.discreteSlider(horizontalSizing, min, max);
	}

	public static SpriteComponent sprite(SpriteIdentifier spriteId) {
		return io.wispforest.owo.ui.component.Components.sprite(spriteId);
	}

	public static SpriteComponent sprite(Sprite sprite) {
		return io.wispforest.owo.ui.component.Components.sprite(sprite);
	}

	public static TextureComponent texture(TextureReference reference) {
		return new TextureComponent(reference);
	}

	public static BoxComponent box(Sizing horizontalSizing, Sizing verticalSizing) {
		return io.wispforest.owo.ui.component.Components.box(horizontalSizing, verticalSizing);
	}

	public static DropdownComponent dropdown(Sizing horizontalSizing) {
		return io.wispforest.owo.ui.component.Components.dropdown(horizontalSizing);
	}

	public static SlimSliderComponent slimSlider(SlimSliderComponent.Axis axis) {
		return io.wispforest.owo.ui.component.Components.slimSlider(axis);
	}

	public static SmallCheckboxComponent smallCheckbox(Text label) {
		return io.wispforest.owo.ui.component.Components.smallCheckbox(label);
	}

	// Utility

	public static <T, C extends Component> FlowLayout list(List<T> data, Consumer<FlowLayout> layoutConfigurator,
			Function<T, C> componentMaker, boolean vertical) {
		return io.wispforest.owo.ui.component.Components.list(data, layoutConfigurator, componentMaker, vertical);
	}

	public static VanillaWidgetComponent wrapVanillaWidget(ClickableWidget widget) {
		return io.wispforest.owo.ui.component.Components.wrapVanillaWidget(widget);
	}

	public static <T extends Component> T createWithSizing(Supplier<T> componentMaker, Sizing horizontalSizing, Sizing verticalSizing) {
		return io.wispforest.owo.ui.component.Components.createWithSizing(componentMaker, horizontalSizing, verticalSizing);
	}

}
