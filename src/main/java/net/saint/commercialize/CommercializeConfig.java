package net.saint.commercialize;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Commercialize.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/emerald_block.png")
public final class CommercializeConfig implements ConfigData {

	// Market

	@ConfigEntry.Category("market")
	@ConfigEntry.Gui.Tooltip
	@Comment("The interval in ticks at which the market checks for expired offers to be removed. (Default: 100 ticks)")
	public int offerTickInterval = 100;

	@ConfigEntry.Category("market")
	@ConfigEntry.Gui.Tooltip
	@Comment("The interval in ticks at which shipping blocks exchange items for currency. (Default: 24,000 ticks/1 day)")
	public int shippingExchangeInterval = 24_000;

	@ConfigEntry.Category("market")
	@ConfigEntry.Gui.Tooltip
	@Comment("The factor applied when buying items from the general market, based off the item's base value. Applies to generated offers. (Default: 1.2)")
	public double buyingPriceFactor = 1.2;

	@ConfigEntry.Category("market")
	@ConfigEntry.Gui.Tooltip
	@Comment("The factor applied when selling items to the general market, based off the item's base value. Applies to the shipping block. (Default: 0.75)")
	public double sellingPriceFactor = 0.75;

	@ConfigEntry.Category("market")
	@ConfigEntry.Gui.Tooltip
	@Comment("The factor used to calculate posting fees for market offers, based off the set offer price. (Default: 0.035)")
	public double postingFeePriceFactor = 0.035;

	@ConfigEntry.Category("market")
	@ConfigEntry.Gui.Tooltip
	@Comment("The factor used to calculate posting fees for market offers, based off the set offer duration. (Default: 0.025)")
	public double postingFeeTimeFactor = 0.025;

	@ConfigEntry.Category("market")
	@ConfigEntry.Gui.Tooltip
	@Comment("The maximum number of offers an individual player can post per in-game day. Limits reset at sunrise. (Default: 16)")
	public int maxNumberOfPlayerOffersPerDay = 16;

	@ConfigEntry.Category("market")
	@ConfigEntry.Gui.Tooltip
	@Comment("The number of ticks in a full in-game day. Used for duration formatting and enforcing limits. (Default: 24,000)")
	public long ticksPerDay = 24_000;

	// Simulation

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("Procedurally generate offers on the market. Simulated players post offers based on templates with custom player names and profiles. (Default: true)")
	public boolean generateOffers = true;

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("The interval in ticks at which new offers are attempted to be generated. (Default: 2,000 ticks)")
	public int offerGenerationTickInterval = 2_000;

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("The maximum number of generated offers that can be active on the market at any time. (Default: 100)")
	public int maxNumberOfOffers = 100;

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("The number of offers that are generated in a single batch. Lower values saturate the market more slowly. (Default: 4)")
	public int offerBatchSize = 4;

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("The factor by which a determined offer price can vary when generated. Applies to generated offers. (Default: 0.1)")
	public double priceJitterFactor = 0.1;

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("The default duration of generated market offers in ticks. (Default: 96,000 ticks = 4 days)")
	public int offerDuration = 96_000;

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("Allow generated and player-created offers to be purchased by simulated players. (Default: false)")
	public boolean generateOfferSales = false;

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("The chance that a simulated offer sale occurs during an offer tick. (Default: 0.02 = 2%)")
	public double offerSaleGenerationChance = 0.02;

	@ConfigEntry.Category("simulation")
	@ConfigEntry.Gui.Tooltip
	@Comment("The highest acceptable price factor for a simulated offer sale compared to to its intrinsic value. Sale chance declines the higher the price divergence. (Default: 1.5)")
	public double offerSaleGenerationMaxPriceFactor = 1.5;

	// Payment

	@ConfigEntry.Category("payment")
	@ConfigEntry.Gui.Tooltip
	@Comment("Enforces the player to have a bound payment card in their inventory for paying from account in the market. (Default: false)")
	public boolean requireCardForMarketPayment = false;

	@ConfigEntry.Category("payment")
	@ConfigEntry.Gui.Tooltip
	@Comment("Allow a player to pay using another player's account if they have a card bound to their account. (Default: true)")
	public boolean allowForeignCardsForMarketPayment = true;

	// Delivery

	@ConfigEntry.Category("delivery")
	@ConfigEntry.Gui.Tooltip
	@Comment("Delivers market orders through the delivery network to a player's mailbox with variable delivery time. (Default: true)")
	public boolean useMailDelivery = true;

	@ConfigEntry.Category("delivery")
	@ConfigEntry.Gui.Tooltip
	@Comment("The average time it takes for a market order to be delivered to a player's mailbox. (Default: 1200)")
	public int mailDeliveryTime = 1_200;

	@ConfigEntry.Category("delivery")
	@ConfigEntry.Gui.Tooltip
	@Comment("The interval in ticks to check if mail is ready to be delivered from the transit queue. (Default: 400)")
	public int mailDeliveryCheckInterval = 400;

	@ConfigEntry.Category("delivery")
	@ConfigEntry.Gui.Tooltip
	@Comment("The effective chance mail gets delivered when out for delivery. Can be decreased from 1.0 (100%) to randomly delay delivery to next tick. (Default: 1.0)")
	public double mailDeliveryChance = 1.0;

	@ConfigEntry.Category("delivery")
	@ConfigEntry.Gui.Tooltip
	@Comment("A string added in the mailbox name to mark it as the main delivery destination for market orders. (Default: '(M)')")
	public String mailboxMainMarker = "(M)";

	@ConfigEntry.Category("delivery")
	@ConfigEntry.Gui.Tooltip
	@Comment("Send text message notifications to players when a delivery adressed to them could not be made (e.g., if they do not have a mailbox). (Default: true)")
	public boolean notifyPlayersOfDeliveryAttempts = true;

	@ConfigEntry.Category("delivery")
	@ConfigEntry.Gui.Tooltip
	@Comment("The maximum number of attempts made to deliver packages to a player if it was previously not possible. (Default: 3)")
	public int maxNumberOfDeliveryAttempts = 3;

	@ConfigEntry.Category("delivery")
	@ConfigEntry.Gui.Tooltip
	@Comment("Do not deliver mail to players that are offline. Packages may be stuck in queue indefinitely if player has pending deliveries but never comes online again. (Default: true)")
	public boolean suspendDeliveryAttemptsForOfflinePlayers = true;

	// Reports

	@ConfigEntry.Category("reports")
	@ConfigEntry.Gui.Tooltip
	@Comment("Send players regular reports on the purchases made and money spent on the market. (Default: false)")
	public boolean sendPlayerMarketBuyReports = false;

	@ConfigEntry.Category("reports")
	@ConfigEntry.Gui.Tooltip
	@Comment("Send players regular reports on the sales made and money earned on the market. (Default: true)")
	public boolean sendPlayerMarketSaleReports = true;

	@ConfigEntry.Category("reports")
	@ConfigEntry.Gui.Tooltip
	@Comment("The interval in ticks to compile and send market reports to players. (Default: 72,000 ticks = 3 days)")
	public long reportInterval = 72_000;

	// GUI

	@ConfigEntry.Category("gui")
	@ConfigEntry.Gui.Tooltip
	@Comment("The maximum number of offers sent to be listed on a market screen before getting truncated. (Default: 100)")
	public int maxNumberOfListedItems = 100;

	@ConfigEntry.Category("gui")
	@ConfigEntry.Gui.Tooltip
	@Comment("The time in ticks after which the market screen automatically refreshes its listing. (Default: 600, 30 seconds)")
	public int listingRefreshInterval = 600;

	@ConfigEntry.Category("gui")
	@ConfigEntry.Gui.Tooltip
	@Comment("The time in ticks after which the market screen automatically refreshes its listing when inactive (no open screen). Will effectively prefetch to have listing ready before player interacts with block. Set to -1 to disable. (Default: 6000, 5 minutes)")
	public int listingRefreshIntervalWhenInactive = 6000;

	// Debug

	@ConfigEntry.Category("debug")
	@ConfigEntry.Gui.Tooltip
	@Comment("Write stats on registered, discovered, and unknown item and fluid values when processing configs. (Default: false)")
	public boolean writeValueDiscoveryStats = false;

}
