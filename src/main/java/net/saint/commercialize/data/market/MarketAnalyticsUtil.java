package net.saint.commercialize.data.market;

import com.mojang.authlib.GameProfile;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.mail.MailTransitUtil;
import net.saint.commercialize.data.market.MarketOfferPostingUtil.OfferDraft;
import net.saint.commercialize.data.offer.Offer;
import net.saint.commercialize.data.text.CurrencyFormattingUtil;
import net.saint.commercialize.data.text.NumericFormattingUtil;
import net.saint.commercialize.init.ModItems;
import net.saint.commercialize.item.LetterItem;
import net.saint.commercialize.screen.posting.OfferPostStrategy;
import net.saint.commercialize.util.LocalizationUtil;

public final class MarketAnalyticsUtil {

	private static long ticksSinceLastReport = 0;

	// Ticking

	public static void tickMarketReportCompilationIfNecessary(ServerWorld world) {
		var server = world.getServer();
		var time = world.getTimeOfDay();

		ticksSinceLastReport += 1;

		// Safeguard against having a server tick-paused on an exact report tick 
		// and then flooding player mailboxes with reports.

		if (time % Commercialize.CONFIG.reportInterval == 0 && ticksSinceLastReport >= 100) {
			tickMarketReportCompilation(server);
			ticksSinceLastReport = 0;
		}
	}

	public static void tickMarketReportCompilation(MinecraftServer server) {
		Commercialize.MARKET_ANALYTICS_MANAGER.getAllReports().forEach(report -> {
			if (Commercialize.CONFIG.sendPlayerMarketBuyReports) {
				compileAndSendBuyReportForPlayer(server, report);
			}

			if (Commercialize.CONFIG.sendPlayerMarketSaleReports) {
				compileAndSendSaleReportForPlayer(server, report);
			}
		});
	}

	public static void compileAndSendBuyReportForPlayer(MinecraftServer server, MarketPlayerReport report) {
		if (report.numberOfOrders == 0) {
			return;
		}

		var senderText = LocalizationUtil.localizedText("text", "delivery.market");
		var itemCountText = localizedItemCountText(report.numberOfOrders);

		var letterSubjectText = LocalizationUtil.localizedText("text", "report.buy.name");
		var letterReportText = LocalizationUtil.localizedText(
				"text", "report.buy.format", report.playerName, itemCountText,
				Text.of(CurrencyFormattingUtil.currencyString(report.amountSpentOnOrders))
		);

		var letterItemStack = makeLetterItemStack(senderText, letterSubjectText, letterReportText);
		var packageItemStacks = defaultedListFromItemStack(letterItemStack);
		var packageMessageText = packageTextForBuyReport();

		MailTransitUtil.packageAndDispatchItemStacksToPlayer(server, report.playerId, packageItemStacks, packageMessageText, senderText);

		report.amountSpentOnOrders = 0;
		report.numberOfOrders = 0;

		Commercialize.MARKET_ANALYTICS_MANAGER.markDirty();
	}

	public static void compileAndSendSaleReportForPlayer(MinecraftServer server, MarketPlayerReport report) {
		if (report.numberOfSales == 0) {
			return;
		}

		var senderText = LocalizationUtil.localizedText("text", "delivery.market");
		var itemCountText = localizedItemCountText(report.numberOfSales);

		var letterSubjectText = LocalizationUtil.localizedText("text", "report.sale.name");
		var letterReportText = LocalizationUtil.localizedText(
				"text", "report.sale.format", report.playerName, itemCountText,
				Text.of(CurrencyFormattingUtil.formatCurrency(report.amountEarnedFromSales))
		);

		var letterItemStack = makeLetterItemStack(senderText, letterSubjectText, letterReportText);
		var packageItemStacks = defaultedListFromItemStack(letterItemStack);
		var packageMessageText = packageTextForSaleReport();

		MailTransitUtil.packageAndDispatchItemStacksToPlayer(server, report.playerId, packageItemStacks, packageMessageText, senderText);

		report.amountEarnedFromSales = 0;
		report.numberOfSales = 0;

		Commercialize.MARKET_ANALYTICS_MANAGER.markDirty();
	}

	private static ItemStack makeLetterItemStack(Text author, Text subject, Text message) {
		var letterStack = new ItemStack(ModItems.LETTER_ITEM);

		LetterItem.putAuthor(letterStack, author);
		LetterItem.putSubject(letterStack, subject);
		LetterItem.putMessage(letterStack, message);

		return letterStack;
	}

	// Report Writing

	public static void writeMarketOrderToAnalytics(Offer offer, GameProfile buyerProfile) {
		if (buyerProfile != null) {
			var buyerReport = Commercialize.MARKET_ANALYTICS_MANAGER.getOrCreateReportForProfile(buyerProfile);

			buyerReport.numberOfOrders += 1;
			buyerReport.amountSpentOnOrders += offer.price;
		}

		var sellerProfile = new GameProfile(offer.sellerId, offer.sellerName);
		var sellerReport = Commercialize.MARKET_ANALYTICS_MANAGER.getOrCreateReportForProfile(sellerProfile);

		sellerReport.numberOfSales += 1;
		sellerReport.amountEarnedFromSales += offer.price;

		Commercialize.MARKET_ANALYTICS_MANAGER.markDirty();
	}

	// Postings

	public static void writeMarketPostingToAnalytics(GameProfile profile, OfferDraft draft) {
		var report = Commercialize.MARKET_ANALYTICS_MANAGER.getOrCreateReportForProfile(profile);
		var numberOfPosts = draft.strategy() == OfferPostStrategy.AS_ITEMS ? draft.stack().getCount() : 1;

		report.numberOfPostings += numberOfPosts;
		report.amountSpentOnFees += draft.fees();

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
