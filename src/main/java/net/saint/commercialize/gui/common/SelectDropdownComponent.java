package net.saint.commercialize.gui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import net.saint.commercialize.gui.Components;
import net.saint.commercialize.library.TextureReference;
import net.saint.commercialize.screen.icons.ScreenAssets;

public class SelectDropdownComponent extends FlowLayout {

	// Library

	public static record Option(String value, String label) {
	}

	// Configuration

	public static final TextureReference CHEVRON_TEXTURE = ScreenAssets.CHEVRON_SMALL_ICON;

	private static final String OPTION_FALLBACK_DESCRIPTION = "--";

	// State

	protected List<Option> options = new ArrayList<>();
	protected @Nullable String selectedValue = null;
	protected @Nullable Consumer<String> onChange = null;

	protected int dropdownWidth = 80;

	// Init

	public SelectDropdownComponent() {
		super(Sizing.fill(100), Sizing.fill(100), Algorithm.HORIZONTAL);

		var containerComponent = Containers.horizontalFlow(Sizing.content(), Sizing.content());

		containerComponent.id("container");
		containerComponent.verticalAlignment(VerticalAlignment.CENTER);
		containerComponent.horizontalAlignment(HorizontalAlignment.RIGHT);
		containerComponent.sizing(Sizing.fill(100), Sizing.fixed(18));
		containerComponent.surface(Surface.PANEL_INSET);
		containerComponent.padding(Insets.of(3));

		var selectedValueLabelComponent = Components.label(Text.of(OPTION_FALLBACK_DESCRIPTION));
		selectedValueLabelComponent.id("selected_value");
		selectedValueLabelComponent.margins(Insets.left(4));

		var chevronComponent = Components.texture(CHEVRON_TEXTURE);
		chevronComponent.sizing(Sizing.fixed(7), Sizing.fixed(5));
		chevronComponent.margins(Insets.of(0, 0, 5, 0));

		containerComponent.child(selectedValueLabelComponent);
		containerComponent.child(chevronComponent);

		containerComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
			if (button == 0) {
				showDropdown();
				return true;
			}

			return false;
		});

		this.child(containerComponent);
	}

	// Properties

	public List<Option> options() {
		return List.copyOf(options);
	}

	public SelectDropdownComponent options(List<Option> options) {
		this.options = new ArrayList<>(options);

		// Restore selection if still part of available options.
		if (this.selectedValue == null || this.options.stream().noneMatch(p -> Objects.equals(p.value(), this.selectedValue))) {
			this.selectedValue = options.isEmpty() ? null : options.get(0).value();
		}

		updateSelectedValueLabel();

		return this;
	}

	public @Nullable String value() {
		return this.selectedValue;
	}

	public SelectDropdownComponent value(@Nullable String newValue) {
		// Pre-check if value is a valid option.
		if (newValue != null && this.options.stream().noneMatch(option -> Objects.equals(option.value(), newValue))) {
			return this;
		}

		this.selectedValue = newValue;
		updateSelectedValueLabel();

		return this;
	}

	public SelectDropdownComponent onChange(Consumer<String> listener) {
		this.onChange = listener;
		return this;
	}

	public int dropdownWidth() {
		return this.dropdownWidth;
	}

	public SelectDropdownComponent dropdownWidth(int width) {
		this.dropdownWidth = width;
		return this;
	}

	// Interaction

	private void showDropdown() {
		var dropdownComponent = Components.dropdown(Sizing.fixed(dropdownWidth));

		dropdownComponent.id("dropdown");
		dropdownComponent.surface(Surface.TOOLTIP);
		dropdownComponent.padding(Insets.of(5));
		dropdownComponent.positioning(Positioning.absolute(this.width() - dropdownWidth - 7, -3));
		dropdownComponent.zIndex(1_000);
		dropdownComponent.closeWhenNotHovered(true);

		addOptionsToDropdown(dropdownComponent, this.options);

		this.child(dropdownComponent);
	}

	private void removeDropdown() {
		var dropdownComponent = childById(DropdownComponent.class, "dropdown");

		if (dropdownComponent != null) {
			dropdownComponent.remove();
		}
	}

	private void addOptionsToDropdown(DropdownComponent dropdown, List<Option> options) {
		if (options.isEmpty()) {
			dropdown.text(Text.of(OPTION_FALLBACK_DESCRIPTION));
			return;
		}

		for (var option : options) {
			var value = option.value();
			var label = option.label();
			var isSelected = option.value().equals(this.selectedValue);

			if (isSelected) {
				label = label + " ✔";
			}

			dropdown.button(Text.of(label), button -> {
				this.selectedValue = value;
				updateSelectedValueLabel();

				if (this.onChange != null) {
					this.onChange.accept(value);
				}

				removeDropdown();
			});
		}
	}

	private void updateSelectedValueLabel() {
		var selectedValueLabelComponent = childById(LabelComponent.class, "selected_value");
		var selectedValueText = Text.of(descriptionForCurrentSelection());

		selectedValueLabelComponent.text(selectedValueText);
	}

	private String descriptionForCurrentSelection() {
		if (this.selectedValue == null) {
			return OPTION_FALLBACK_DESCRIPTION;
		}

		for (var option : this.options) {
			var value = option.value();

			if (Objects.equals(value, this.selectedValue)) {
				return option.label();
			}
		}

		return OPTION_FALLBACK_DESCRIPTION;
	}

}