package net.saint.commercialize.data.offer;

import net.minecraft.util.Identifier;
import net.saint.commercialize.data.common.Availability;
import net.saint.commercialize.data.common.StackSizeRange;

public class OfferTemplate {

	// Properties

	public Identifier item;
	public StackSizeRange stack;
	public float markup = 1.0f;
	public Availability availability = Availability.COMMON;

}
