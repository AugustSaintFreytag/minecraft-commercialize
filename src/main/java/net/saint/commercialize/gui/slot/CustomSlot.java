package net.saint.commercialize.gui.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class CustomSlot extends Slot {

	// Properties

	private boolean isInteractible = true;

	// Init

	public CustomSlot(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	// Interaction

	public void setInteractible(boolean interactible) {
		this.isInteractible = interactible;
	}

	@Override
	public boolean canBeHighlighted() {
		return isInteractible;
	}

}
