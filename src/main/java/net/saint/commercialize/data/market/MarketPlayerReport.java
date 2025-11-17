package net.saint.commercialize.data.market;

import java.util.UUID;

public class MarketPlayerReport {

	// Properties

	public UUID playerId;
	public String playerName;

	public int numberOfSales = 0;
	public int amountEarnedFromSales = 0;

	public int numberOfPostings = 0;
	public int amountSpentOnFees = 0;

	public int numberOfOrders = 0;
	public int amountSpentOnOrders = 0;

	// Decoding

	public static MarketPlayerReport fromNbt(net.minecraft.nbt.NbtCompound nbt) {
		var report = new MarketPlayerReport();

		report.playerId = nbt.getUuid("playerId");
		report.playerName = nbt.getString("playerName");
		report.numberOfSales = nbt.getInt("numberOfSales");
		report.amountEarnedFromSales = nbt.getInt("amountEarnedFromSales");
		report.numberOfPostings = nbt.getInt("numberOfPostings");
		report.amountSpentOnFees = nbt.getInt("amountSpentOnFees");
		report.numberOfOrders = nbt.getInt("numberOfOrders");
		report.amountSpentOnOrders = nbt.getInt("amountSpentOnOrders");

		return report;
	}

	// Encoding

	public net.minecraft.nbt.NbtCompound writeNbt(net.minecraft.nbt.NbtCompound nbt) {
		nbt.putUuid("playerId", playerId);
		nbt.putString("playerName", playerName);
		nbt.putInt("numberOfSales", numberOfSales);
		nbt.putInt("amountEarnedFromSales", amountEarnedFromSales);
		nbt.putInt("numberOfPostings", numberOfPostings);
		nbt.putInt("amountSpentOnFees", amountSpentOnFees);
		nbt.putInt("numberOfOrders", numberOfOrders);
		nbt.putInt("amountSpentOnOrders", amountSpentOnOrders);

		return nbt;
	}

}
