package net.saint.commercialize.data.market;

import com.mojang.authlib.GameProfile;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.mail.MailTransitUtil;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;
import net.saint.commercialize.data.text.NumericFormattingUtil;
import net.saint.commercialize.data.text.TimeFormattingUtil;
import net.saint.commercialize.init.ModItems;
import net.saint.commercialize.item.LetterItem;
import net.saint.commercialize.util.LocalizationUtil;

public final class MarketAnalyticsUtil {

	private static long ticksSinceLastKnownReport = 0;

	private static int numberOfReportsSentInLastTick = 0;

	// Access

	public static int getNumberOfReportsSentInLastTick() {
		return numberOfReportsSentInLastTick;
	}

	// Ticking

	public static void compileAndSendMarketReportsIfNecessary(ServerWorld world) {
		var server = world.getServer();
		var time = world.getTimeOfDay();

		ticksSinceLastKnownReport += 1;

		// Safeguard against having a server tick-paused on an exact report tick
		// and then flooding player mailboxes with reports (min. 1 minute pacing).

		if (time % Commercialize.CONFIG.reportInterval == 0 && ticksSinceLastKnownReport >= 1200) {
			compileAndSendMarketReports(server);
			ticksSinceLastKnownReport = 0;
		}
	}

	public static void compileAndSendMarketReports(MinecraftServer server) {
		numberOfReportsSentInLastTick = 0;

		Commercialize.MARKET_ANALYTICS_MANAGER.getAllIntervalReports().forEach(report -> {
			if (Commercialize.CONFIG.sendPlayerMarketBuyReports) {
				compileAndSendBuyReport(server, report);
			}

			if (Commercialize.CONFIG.sendPlayerMarketSaleReports) {
				compileAndSendSaleReport(server, report);
			}
		});
	}

	public static void compileAndSendBuyReport(MinecraftServer server, MarketPlayerReport report) {
		if (report.numberOfOrders == 0) {
			return;
		}

		var profile = report.getGameProfile();

		var senderText = LocalizationUtil.localizedText("text", "delivery.market");
		var itemCountText = localizedItemCountText(report.numberOfOrders);

		var intervalText = TimeFormattingUtil.formattedTime(Commercialize.CONFIG.reportInterval);
		var letterSubjectText = LocalizationUtil.localizedText("text", "report.buy.name");
		var letterReportText = LocalizationUtil.localizedText("text", "report.buy.format", report.playerName, intervalText, itemCountText,
				CurrencyFormattingUtil.currencyText(report.amountSpentOnOrders));

		var letterItemStack = makeLetterItemStack(senderText, letterSubjectText, letterReportText);
		var packageItemStacks = defaultedListFromItemStack(letterItemStack);
		var packageMessageText = packageTextForBuyReport();

		MailTransitUtil.packageAndDispatchItemStacksToPlayer(server, report.playerId, packageItemStacks, packageMessageText, senderText);
		numberOfReportsSentInLastTick++;

		report.clearBuyerSide();
		Commercialize.MARKET_ANALYTICS_MANAGER.markDirty();
	}

	public static void compileAndSendSaleReport(MinecraftServer server, MarketPlayerReport report) {
		if (report.numberOfPostingsSold == 0 && report.numberOfPostingsExpired == 0) {
			return;
		}

		var amountSpentOnPostings = report.amountSpentOnPostingsSold + report.amountSpentOnPostingsExpired;
		var amountEarnedFromSales = report.amountEarnedFromSales;
		var netEarningsAmount = amountEarnedFromSales - amountSpentOnPostings;

		var senderText = LocalizationUtil.localizedText("text", "delivery.market");
		var intervalText = TimeFormattingUtil.formattedTime(Commercialize.CONFIG.reportInterval);
		var offersPostedCountText = localizedItemCountText(report.numberOfPostingsSold + report.numberOfPostingsExpired);
		var offersSoldCountText = localizedItemCountText(report.numberOfPostingsSold);
		var feesPaidText = CurrencyFormattingUtil.currencyText(amountSpentOnPostings);
		var grossEarningsText = CurrencyFormattingUtil.currencyText(amountEarnedFromSales);
		var netEarningsText = CurrencyFormattingUtil.currencyText(netEarningsAmount);

		var letterSubjectText = LocalizationUtil.localizedText("text", "report.sale.name");
		var letterReportText = LocalizationUtil.localizedText("text", "report.sale.format", report.playerName, intervalText,
				offersPostedCountText, offersSoldCountText, feesPaidText, grossEarningsText, netEarningsText);

		var letterItemStack = makeLetterItemStack(senderText, letterSubjectText, letterReportText);
		var packageItemStacks = defaultedListFromItemStack(letterItemStack);
		var packageMessageText = packageTextForSaleReport();

		MailTransitUtil.packageAndDispatchItemStacksToPlayer(server, report.playerId, packageItemStacks, packageMessageText, senderText);
		numberOfReportsSentInLastTick++;

		report.clearSellerSide();
		Commercialize.MARKET_ANALYTICS_MANAGER.markDirty();
	}

	private static ItemStack makeLetterItemStack(Text author, Text subject, Text message) {
		var letterStack = new ItemStack(ModItems.LETTER_ITEM);

		LetterItem.putAuthor(letterStack, author);
		LetterItem.putSubject(letterStack, subject);
		LetterItem.putMessage(letterStack, message);

		return letterStack;
	}

	// Orders

	public static void writeMarketOrderToAnalytics(Offer offer, GameProfile buyerProfile) {
		if (buyerProfile != null) {
			var buyerReport = Commercialize.MARKET_ANALYTICS_MANAGER.getOrCreateIntervalReportForProfile(buyerProfile);

			buyerReport.numberOfOrders += 1;
			buyerReport.amountSpentOnOrders += offer.price;
		}

		var sellerProfile = new GameProfile(offer.sellerId, offer.sellerName);
		var sellerReport = Commercialize.MARKET_ANALYTICS_MANAGER.getOrCreateIntervalReportForProfile(sellerProfile);

		sellerReport.numberOfPostingsSold += 1;
		sellerReport.amountEarnedFromSales += offer.price;
		sellerReport.amountSpentOnPostingsSold += offer.fees;

		Commercialize.MARKET_ANALYTICS_MANAGER.markDirty();
	}

	// Postings

	public static void writeMarketExpirationToAnalytics(GameProfile profile, Offer offer) {
		var report = Commercialize.MARKET_ANALYTICS_MANAGER.getOrCreateIntervalReportForProfile(profile);

		report.numberOfPostingsExpired += 1;
		report.amountSpentOnPostingsExpired += offer.fees;

		Commercialize.MARKET_ANALYTICS_MANAGER.markDirty();
	}

	// Utilities

	private static DefaultedList<ItemStack> defaultedListFromItemStack(ItemStack stack) {
		var list = DefaultedList.ofSize(1, ItemStack.EMPTY);
		list.set(0, stack);

		return list;
	}

	private static Text localizedItemCountText(int itemCount) {
		var text = Text.empty().append(Text.of(NumericFormattingUtil.formatNumber(itemCount))).append(Text.of(" "));

		if (itemCount == 1) {
			return text.append(LocalizationUtil.localizedText("text", "unit.item"));
		} else {
			return text.append(LocalizationUtil.localizedText("text", "unit.items"));
		}
	}

	private static Text packageTextForBuyReport() {
		var text = Text.empty();

		text.append(LocalizationUtil.localizedText("text", "report.buy.name")).append(Text.of("\n\n"));
		text.append(LocalizationUtil.localizedText("text", "report.buy.message"));

		return text;
	}

	private static Text packageTextForSaleReport() {
		var text = Text.empty();

		text.append(LocalizationUtil.localizedText("text", "report.sale.name")).append(Text.of("\n\n"));
		text.append(LocalizationUtil.localizedText("text", "report.sale.message"));

		return text;
	}

}
