package net.saint.commercialize.util.db;

public final class DatabaseSetUpStatements {

	// Offers Table

	public static final String CREATE_OFFERS_TABLE_STATEMENT = """
				CREATE TABLE IF NOT EXISTS "offers" (
					"id" TEXT PRIMARY KEY,
					"is_active" BOOLEAN NOT NULL,
					"is_generated" BOOLEAN NOT NULL,
					"seller_id" TEXT NOT NULL,
					"seller_name" TEXT NOT NULL,
					"stack" TEXT NOT NULL, -- Full encoded stack, including item, nbt, count
					"stack_name" TEXT NOT NULL, -- Normalized localized stack name
					"price" INTEGER NOT NULL,
					"duration" INTEGER NOT NULL,
					"time_posted_tick" INTEGER NOT NULL,
					"time_posted_time" INTEGER NOT NULL
				)
			""";

	public static final String CREATE_OFFERS_INDICES_STATEMENT = """
				CREATE INDEX IF NOT EXISTS "index_offers_active_by_time" ON "offers" ("is_active", "stack_name", "time_posted_tick");
				CREATE INDEX IF NOT EXISTS "index_offers_active_by_price" ON "offers" ("is_active", "stack_name", "price");
				CREATE INDEX IF NOT EXISTS "index_offers_active_by_seller_name" ON "offers" ("is_active", "stack_name", "seller_name");
			""";

	// Transit Table

	public static final String CREATE_TRANSIT_TABLE_STATEMENT = """
				CREATE TABLE IF NOT EXISTS "transit" (
					"id" TEXT PRIMARY KEY,
					"recipient_id" TEXT NOT NULL,
					"recipient_name" TEXT NOT NULL,
					"stack" TEXT NOT NULL,
					"time_dispatched_tick" INTEGER NOT NULL,
					"time_dispatched_time" INTEGER NOT NULL,
					"time_last_delivery_attempted_tick" INTEGER,
					"time_last_delivery_attempted_time" INTEGER,
					"number_of_delivery_attempts" INTEGER NOT NULL
				);
			""";

	public static final String CREATE_TRANSIT_INDICES_STATEMENT = """
				CREATE INDEX IF NOT EXISTS "index_transit_by_recipient" ON "transit" ("recipient_id", "time_dispatched_tick", "time_last_delivery_attempted_tick");
			""";

	// Transactions Table

	public static final String CREATE_TRANSACTIONS_TABLE_STATEMENT = """
				CREATE TABLE IF NOT EXISTS "transactions" (
					"id" TEXT PRIMARY KEY,
					"seller_id" TEXT NOT NULL,
					"seller_name" TEXT NOT NULL,
					"buyer_id" TEXT NOT NULL,
					"buyer_name" TEXT NOT NULL,
					"stack" TEXT NOT NULL,
					"stack_name" TEXT NOT NULL,
					"price" INTEGER NOT NULL,
					"offer_is_generated" BOOLEAN NOT NULL,
					"sale_is_generated" BOOLEAN NOT NULL,
					"time_posted_tick" INTEGER NOT NULL,
					"time_posted_time" INTEGER NOT NULL,
					"time_sold_tick" INTEGER NOT NULL,
					"time_sold_time" INTEGER NOT NULL
				);
			""";

	public static final String CREATE_TRANSACTIONS_INDICES_STATEMENT = """
				CREATE INDEX IF NOT EXISTS "index_transactions_by_buyer" ON "transactions" ("buyer_id", "time_sold_tick");
				CREATE INDEX IF NOT EXISTS "index_transactions_by_seller" ON "transactions" ("seller_id", "time_sold_tick");
			""";

}
