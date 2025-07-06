package net.saint.commercialize.init;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.BoolArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.market.MarketOfferTickingUtil;

public final class ModCommands {

	// Configuration

	private static final int NUMBER_OF_GENERATIONS_PER_COMMAND = 4;

	// Init

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->

		dispatcher.register(literal(Commercialize.MOD_ID)

				.then(literal("clearOffers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					Commercialize.MARKET_MANAGER.clearOffers();
					return 1;
				}))

				.then(literal("clearAndRegenerateOffers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var server = context.getSource().getServer();
					var world = server.getOverworld();

					Commercialize.MARKET_MANAGER.clearOffers();

					for (var i = 0; i < NUMBER_OF_GENERATIONS_PER_COMMAND; i++) {
						MarketOfferTickingUtil.tickMarketOfferGeneration(world);
					}

					return 1;
				}))

				.then(literal("generateOffers").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
					var server = context.getSource().getServer();
					var world = server.getOverworld();
					MarketOfferTickingUtil.tickMarketOfferGeneration(world);

					return 1;
				}))

				.then(literal("doMarketTicking").requires(source -> source.hasPermissionLevel(4))
						.then(argument("state", BoolArgumentType.bool())).executes(context -> {
							var state = BoolArgumentType.getBool(context, "state");
							Commercialize.shouldTickMarket = state;
							return 1;
						}))));

	}
}
