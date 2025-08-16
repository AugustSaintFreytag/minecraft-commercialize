package net.saint.commercialize.gui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.LabelComponent;
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

public class SelectDropdownComponent<Value> extends FlowLayout {

	// Library

	public static record Option<Value>(Value value, String label) {
	}

	// Configuration

	public static final TextureReference CHEVRON_TEXTURE = ScreenAssets.CHEVRON_SMALL_ICON;

	private static final String OPTION_FALLBACK_DESCRIPTION = "--";

	// State

	protected List<Option<Value>> options = new ArrayList<>();
	protected @Nullable Value selectedValue = null;

	protected @Nullable Supplier<FlowLayout> openOverlay = null;
	protected @Nullable Runnable closeOverlay = null;

	protected @Nullable Consumer<Value> onChange = null;

	protected int popoverWidth = 80;

	// Init

	public SelectDropdownComponent() {
		super(Sizing.fill(100), Sizing.fill(100), Algorithm.HORIZONTAL);

		verticalAlignment(VerticalAlignment.CENTER);
		horizontalAlignment(HorizontalAlignment.LEFT);
		surface(Surface.VANILLA_TRANSLUCENT);
		padding(Insets.of(3));

		var selectedValueLabelComponent = Components.label(Text.of(OPTION_FALLBACK_DESCRIPTION));
		selectedValueLabelComponent.id("selected_value");
		selectedValueLabelComponent.sizing(Sizing.content());
		selectedValueLabelComponent.margins(Insets.left(4));
		selectedValueLabelComponent.shadow(true);

		var chevronComponent = Components.texture(CHEVRON_TEXTURE);
		chevronComponent.sizing(Sizing.fixed(7), Sizing.fixed(5));
		chevronComponent.margins(Insets.of(0, 0, 0, 2));

		child(chevronComponent);
		child(selectedValueLabelComponent);

		mouseDown().subscribe((mouseX, mouseY, button) -> {
			if (button == 0) {
				openDropdown(mouseX, mouseY);
				return true;
			}

			return false;
		});
	}

	// Properties

	public List<Option<Value>> options() {
		return List.copyOf(options);
	}

	public SelectDropdownComponent<Value> options(List<Option<Value>> options) {
		this.options = new ArrayList<>(options);

		// Restore selection if still part of available options.
		if (this.selectedValue == null || this.options.stream().noneMatch(p -> Objects.equals(p.value(), this.selectedValue))) {
			this.selectedValue = options.isEmpty() ? null : options.get(0).value();
		}

		updateSelectedValueLabel();

		return this;
	}

	public @Nullable Value value() {
		return this.selectedValue;
	}

	public SelectDropdownComponent<Value> value(@Nullable Value newValue) {
		// Pre-check if value is a valid option.
		if (newValue != null && this.options.stream().noneMatch(option -> Objects.equals(option.value(), newValue))) {
			return this;
		}

		this.selectedValue = newValue;
		updateSelectedValueLabel();

		return this;
	}

	public void onOpenOverlay(Supplier<FlowLayout> overlaySupplier) {
		this.openOverlay = overlaySupplier;
	}

	public void onCloseOverlay(Runnable closeHandler) {
		this.closeOverlay = closeHandler;
	}

	public int dropdownWidth() {
		return this.popoverWidth;
	}

	public SelectDropdownComponent<Value> popoverWidth(int width) {
		this.popoverWidth = width;
		return this;
	}

	// Interaction

	private void openDropdown(double mouseX, double mouseY) {
		var dropdownComponent = Components.dropdown(Sizing.fixed(popoverWidth));

		dropdownComponent.id("dropdown");
		dropdownComponent.surface(Surface.TOOLTIP);
		dropdownComponent.padding(Insets.of(0));
		dropdownComponent.positioning(Positioning.absolute(this.x() + this.width() - popoverWidth - 7, this.y() - 3));
		dropdownComponent.closeWhenNotHovered(false);

		addOptionsToDropdown(dropdownComponent, this.options);

		var hostComponent = openOverlay.get();
		hostComponent.child(dropdownComponent);
	}

	private void closeDropdown() {
		closeOverlay.run();
	}

	private void addOptionsToDropdown(DropdownComponent dropdown, List<Option<Value>> options) {
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

				closeDropdown();
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