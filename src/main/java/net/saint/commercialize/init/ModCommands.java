package net.saint.commercialize.init;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.block.shipping.ShippingBlockEntity;
import net.saint.commercialize.data.mail.MailTransitUtil;
import net.saint.commercialize.data.market.MarketOfferTickingUtil;
import net.saint.commercialize.data.shipping.ShippingExchangeTickingUtil;
import net.saint.commercialize.data.shipping.ShippingExchangeTickingUtil.ShippingTickResult;
import net.saint.commercialize.util.WorldUtil;

public final class ModCommands {

	// Configuration

	private static final int NUMBER_OF_GENERATIONS_PER_COMMAND = 4;
	private static final int CHUNK_RADIUS_FOR_ENTITY_INTERACTION = 4;

	// Init

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->

		dispatcher.register(
				literal(Commercialize.MOD_ID)

						// Mod

						.then(literal("reload").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
							var server = context.getSource().getServer();
							Commercialize.reloadConfigs(server);
							context.getSource().sendFeedback(() -> Text.literal("Commercialize configuration files reloaded."), true);

							return 1;
						}))

						// Value

						.then(literal("queryValue").then(argument("id", StringArgumentType.string()).executes(context -> {
							var rawItemId = StringArgumentType.getString(context, "id");
							var itemId = Identifier.tryParse(rawItemId);

							if (itemId == null) {
								context.getSource().sendError(
										Text.literal("Invalid item identifier: '" + rawItemId + "'.")
								);
								return 0;
							}

							var value = Commercialize.ITEM_MANAGER.getValueForItem(itemId);

							if (value == 0) {
								context.getSource().sendError(
										Text.literal("No value registered for item: '" + itemId + "'.")
								);
								return 0;
							}

							context.getSource().sendFeedback(
									() -> Text.literal("Value for " + itemId + ": " + value),
									false
							);

							return 1;
						})))

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

							context.getSource().sendFeedback(
									() -> Text.literal(
											"Cleared " + numberOfOffers
													+ " generated market offer(s), configured cap: "
													+ Commercialize.CONFIG.maxNumberOfOffers + " offer(s)."
									),
									true
							);

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
											() -> Text.literal(
													"All market offers cleared, generated " + NUMBER_OF_GENERATIONS_PER_COMMAND
															+ " new offer(s), configured cap: " + Commercialize.CONFIG.maxNumberOfOffers
															+ " offer(s)."
											),
											true
									);
							return 1;
						}))

						.then(literal("generateOffers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
							var server = context.getSource().getServer();
							var world = server.getOverworld();

							var numberOfOffers = Commercialize.MARKET_OFFER_MANAGER.size();

							MarketOfferTickingUtil.tickMarketOfferGeneration(world);

							var addedNumberOfOffers = Commercialize.MARKET_OFFER_MANAGER.size() - numberOfOffers;

							context.getSource().sendFeedback(
									() -> Text.literal(
											"Generated " + addedNumberOfOffers
													+ " new market offer(s), configured cap: " + Commercialize.CONFIG.maxNumberOfOffers
													+ " offer(s)."
									),
									true
							);
							return 1;
						}))

						.then(
								literal("cycleNearbyShippingBoxes").requires(source -> source.hasPermissionLevel(4))
										.then(argument("player", EntityArgumentType.player()).executes(context -> {
											var world = context.getSource().getWorld();
											var player = EntityArgumentType.getPlayer(context, "player");
											var playerPosition = player.getBlockPos();
											var numberOfSuccessfullyTickedShippingBoxes = new AtomicInteger(0);
											var numberOfFailedTickedShippingBoxes = new AtomicInteger(0);

											WorldUtil.forEachChunkAroundPosition(
													world,
													playerPosition,
													CHUNK_RADIUS_FOR_ENTITY_INTERACTION,
													chunk -> {
														WorldUtil.forEachBlockEntityInChunk(
																chunk,
																ShippingBlockEntity.class,
																blockEntity -> {
																	ShippingExchangeTickingUtil.tickShipping(world, blockEntity, result -> {
																		if (result == ShippingTickResult.SOLD) {
																			numberOfSuccessfullyTickedShippingBoxes.getAndIncrement();
																		} else if (result == ShippingTickResult.FAILURE) {
																			numberOfFailedTickedShippingBoxes.getAndIncrement();
																		}
																	});
																}
														);
													}
											);

											var totalNumberOfTickedShippingBoxes = numberOfSuccessfullyTickedShippingBoxes.get()
													+ numberOfFailedTickedShippingBoxes.get();

											if (totalNumberOfTickedShippingBoxes > 0) {
												context.getSource()
														.sendFeedback(
																() -> Text.literal(
																		"Ticked " + numberOfSuccessfullyTickedShippingBoxes.get()
																				+ " shipping boxes ("
																				+ numberOfFailedTickedShippingBoxes.get() + " failed)."
																),
																true
														);
											} else {
												context.getSource()
														.sendFeedback(() -> Text.literal("No shipping boxes found in vicinity."), true);
											}

											return 1;
										}))
						)

						.then(
								literal("doOfferTicking").requires(source -> source.hasPermissionLevel(4))
										.then(argument("state", BoolArgumentType.bool())).executes(context -> {
											var state = BoolArgumentType.getBool(context, "state");
											Commercialize.shouldTickMarket = state;

											context.getSource().sendFeedback(
													() -> Text.literal("Market offer ticking is now set to: " + state + "."),
													true
											);
											return 1;
										})
						)

						// Mail

						.then(literal("clearGlobalMailQueue").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
							var server = context.getSource().getServer();
							var numberOfMailInTransit = Commercialize.MAIL_TRANSIT_MANAGER.getItems().count();
							MailTransitUtil.forceDeliverAllMailTransitItems(server);
							var numberOfMailDelivered = numberOfMailInTransit - Commercialize.MAIL_TRANSIT_MANAGER.getItems().count();

							Commercialize.MAIL_TRANSIT_MANAGER.clearItems();
							context.getSource()
									.sendFeedback(
											() -> Text.literal(
													"Mail transit queue cleared for all players (" + numberOfMailDelivered
															+ " item(s) force-delivered)."
											),
											true
									);
							return 1;
						}))

						.then(
								literal("clearMailQueue").requires(source -> source.hasPermissionLevel(4))
										.then(argument("player", EntityArgumentType.player()).executes(context -> {
											var server = context.getSource().getServer();
											var player = EntityArgumentType.getPlayer(context, "player");
											var pendingTransitItems = Commercialize.MAIL_TRANSIT_MANAGER.getItems().filter(transitItem -> {
												return transitItem.recipient == player.getUuid();
											});

											pendingTransitItems.forEach(transitItem -> {
												MailTransitUtil.deliverMailTransitItem(server, transitItem);
												Commercialize.MAIL_TRANSIT_MANAGER.removeItem(transitItem);
											});

											context.getSource()
													.sendFeedback(
															() -> Text
																	.literal("Mail transit queue cleared for player: " + player.getName()),
															true
													);
											return 1;
										}))
						)
		));

	}
}
