package net.saint.commercialize.init;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.BoolArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.text.Text;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.market.MarketOfferTickingUtil;

public final class ModCommands {

	// Configuration

	private static final int NUMBER_OF_GENERATIONS_PER_COMMAND = 4;

	// Init

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->

		dispatcher.register(literal(Commercialize.MOD_ID)

				// Mod

				.then(literal("reload").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var server = context.getSource().getServer();
					Commercialize.reloadConfigs(server);
					context.getSource().sendFeedback(() -> Text.literal("Commercialize configuration files reloaded."), true);
					return 1;
				}))

				// Offers

				.then(literal("clearOffers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var world = context.getSource().getWorld();
					var numberOfOffers = Commercialize.MARKET_OFFER_MANAGER.size();

					Commercialize.MARKET_OFFER_MANAGER.getOffers().forEach(offer -> {
						MarketOfferTickingUtil.expireAndRemoveOffer(world, offer);
					});

					context.getSource().sendFeedback(() -> Text.literal("Cleared " + numberOfOffers + " market offer(s)."), true);

					return 1;
				}))

				.then(literal("clearGeneratedOffers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var world = context.getSource().getWorld();
					var offers = Commercialize.MARKET_OFFER_MANAGER.getOffers().filter(offer -> offer.isGenerated).toList();
					var numberOfOffers = offers.size();

					offers.forEach(offer -> {
						MarketOfferTickingUtil.expireAndRemoveOffer(world, offer);
					});

					context.getSource().sendFeedback(() -> Text.literal("Cleared " + numberOfOffers
							+ " generated market offer(s), configured cap: " + Commercialize.CONFIG.maxNumberOfOffers + " offer(s)."),
							true);

					return 1;
				}))

				.then(literal("clearAndRegenerateOffers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var world = context.getSource().getWorld();

					Commercialize.MARKET_OFFER_MANAGER.getOffers().forEach(offer -> {
						MarketOfferTickingUtil.expireAndRemoveOffer(world, offer);
					});

					for (var i = 0; i < NUMBER_OF_GENERATIONS_PER_COMMAND; i++) {
						MarketOfferTickingUtil.tickMarketOfferGeneration(world);
					}

					context.getSource()
							.sendFeedback(
									() -> Text.literal("All market offers cleared, generated " + NUMBER_OF_GENERATIONS_PER_COMMAND
											+ " new offer(s), configured cap: " + Commercialize.CONFIG.maxNumberOfOffers + " offer(s)."),
									true);
					return 1;
				}))

				.then(literal("generateOffers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var server = context.getSource().getServer();
					var world = server.getOverworld();

					var numberOfOffers = Commercialize.MARKET_OFFER_MANAGER.size();

					MarketOfferTickingUtil.tickMarketOfferGeneration(world);

					var addedNumberOfOffers = Commercialize.MARKET_OFFER_MANAGER.size() - numberOfOffers;

					context.getSource().sendFeedback(() -> Text.literal("Generated " + addedNumberOfOffers
							+ " new market offer(s), configured cap: " + Commercialize.CONFIG.maxNumberOfOffers + " offer(s)."), true);
					return 1;
				}))

				.then(literal("doOfferTicking").requires(source -> source.hasPermissionLevel(4))
						.then(argument("state", BoolArgumentType.bool())).executes(context -> {
							var state = BoolArgumentType.getBool(context, "state");
							Commercialize.shouldTickMarket = state;

							context.getSource().sendFeedback(() -> Text.literal("Market offer ticking is now set to: " + state + "."),
									true);
							return 1;
						}))

				// Mail

				.then(literal("clearGlobalMailQueue").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					Commercialize.MAIL_TRANSIT_MANAGER.clearItems();
					context.getSource().sendFeedback(() -> Text.literal("Mail transit queue cleared for all players."), true);
					return 1;
				}))

				.then(literal("clearMailQueue").requires(source -> source.hasPermissionLevel(4))
						.then(argument("player", EntityArgumentType.player()).executes(context -> {
							var player = EntityArgumentType.getPlayer(context, "player");
							var pendingTransitItems = Commercialize.MAIL_TRANSIT_MANAGER.getItems().filter(transitItem -> {
								return transitItem.recipient == player.getUuid();
							});

							pendingTransitItems.forEach(transitItem -> {
								Commercialize.MAIL_TRANSIT_MANAGER.removeItem(transitItem);
							});

							context.getSource()
									.sendFeedback(() -> Text.literal("Mail transit queue cleared for player: " + player.getName()), true);
							return 1;
						})))));

	}
}
