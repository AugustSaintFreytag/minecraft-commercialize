package net.saint.commercialize.data.offer;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;

public class Offer {

	// Properties

	public UUID id;

	public boolean isActive;
	public boolean isGenerated;

	@Nullable
	public UUID sellerId;

	@Nullable
	public String sellerName;

	public long timestamp;
	public int duration;

	public ItemStack stack;
	public int price;

}