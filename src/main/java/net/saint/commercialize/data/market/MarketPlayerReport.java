package net.saint.commercialize.data.market;

import java.util.UUID;

import net.minecraft.nbt.NbtCompound;

public class MarketPlayerReport {

	// Keys

	private static final String PLAYER_ID_NBT_KEY = "PlayerId";
	private static final String PLAYER_NAME_NBT_KEY = "PlayerName";

	private static final String NUMBER_OF_POSTINGS_SOLD_NBT_KEY = "NumberOfPostingsSold";
	private static final String NUMBER_OF_POSTINGS_OPEN_NBT_KEY = "NumberOfPostingsOpen";
	private static final String NUMBER_OF_POSTINGS_EXPIRED_NBT_KEY = "NumberOfPostingsExpired";
	private static final String AMOUNT_EARNED_FROM_SALES_NBT_KEY = "AmountEarnedFromSales";
	private static final String AMOUNT_SPENT_ON_POSTINGS_SOLD_NBT_KEY = "AmountSpentOnPostingsSold";
	private static final String AMOUNT_SPENT_ON_POSTINGS_EXPIRED_NBT_KEY = "AmountSpentOnPostingsExpired";

	private static final String NUMBER_OF_ORDERS_NBT_KEY = "NumberOfOrders";
	private static final String AMOUNT_SPENT_ON_ORDERS_NBT_KEY = "AmountSpentOnOrders";

	// Properties

	public UUID playerId;
	public String playerName;

	public int numberOfPostingsSold = 0;
	public int numberOfPostingsOpen = 0;
	public int numberOfPostingsExpired = 0;

	public int amountEarnedFromSales = 0;
	public int amountSpentOnPostingsSold = 0;
	public int amountSpentOnPostingsExpired = 0;

	public int numberOfOrders = 0;
	public int amountSpentOnOrders = 0;

	// Decoding

	public static MarketPlayerReport fromNbt(NbtCompound nbt) {
		var report = new MarketPlayerReport();

		report.playerId = nbt.getUuid(PLAYER_ID_NBT_KEY);
		report.playerName = nbt.getString(PLAYER_NAME_NBT_KEY);
		report.numberOfPostingsSold = nbt.getInt(NUMBER_OF_POSTINGS_SOLD_NBT_KEY);
		report.numberOfPostingsOpen = nbt.getInt(NUMBER_OF_POSTINGS_OPEN_NBT_KEY);
		report.numberOfPostingsExpired = nbt.getInt(NUMBER_OF_POSTINGS_EXPIRED_NBT_KEY);
		report.amountEarnedFromSales = nbt.getInt(AMOUNT_EARNED_FROM_SALES_NBT_KEY);
		report.amountSpentOnPostingsSold = nbt.getInt(AMOUNT_SPENT_ON_POSTINGS_SOLD_NBT_KEY);
		report.amountSpentOnPostingsExpired = nbt.getInt(AMOUNT_SPENT_ON_POSTINGS_EXPIRED_NBT_KEY);
		report.numberOfOrders = nbt.getInt(NUMBER_OF_ORDERS_NBT_KEY);
		report.amountSpentOnOrders = nbt.getInt(AMOUNT_SPENT_ON_ORDERS_NBT_KEY);

		return report;
	}

	// Encoding

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putUuid(PLAYER_ID_NBT_KEY, playerId);
		nbt.putString(PLAYER_NAME_NBT_KEY, playerName);
		nbt.putInt(NUMBER_OF_POSTINGS_SOLD_NBT_KEY, numberOfPostingsSold);
		nbt.putInt(NUMBER_OF_POSTINGS_OPEN_NBT_KEY, numberOfPostingsOpen);
		nbt.putInt(NUMBER_OF_POSTINGS_EXPIRED_NBT_KEY, numberOfPostingsExpired);
		nbt.putInt(AMOUNT_EARNED_FROM_SALES_NBT_KEY, amountEarnedFromSales);
		nbt.putInt(AMOUNT_SPENT_ON_POSTINGS_SOLD_NBT_KEY, amountSpentOnPostingsSold);
		nbt.putInt(AMOUNT_SPENT_ON_POSTINGS_EXPIRED_NBT_KEY, amountSpentOnPostingsExpired);
		nbt.putInt(NUMBER_OF_ORDERS_NBT_KEY, numberOfOrders);
		nbt.putInt(AMOUNT_SPENT_ON_ORDERS_NBT_KEY, amountSpentOnOrders);

		return nbt;
	}

	// Merge

	public void union(MarketPlayerReport report) {
		this.numberOfPostingsSold += report.numberOfPostingsSold;
		this.numberOfPostingsOpen += report.numberOfPostingsOpen;
		this.numberOfPostingsExpired += report.numberOfPostingsExpired;
		this.amountEarnedFromSales += report.amountEarnedFromSales;
		this.amountSpentOnPostingsSold += report.amountSpentOnPostingsSold;
		this.amountSpentOnPostingsExpired += report.amountSpentOnPostingsExpired;
		this.numberOfOrders += report.numberOfOrders;
		this.amountSpentOnOrders += report.amountSpentOnOrders;
	}

}
