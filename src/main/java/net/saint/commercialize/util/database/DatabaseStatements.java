package net.saint.commercialize.util.database;

public final class DatabaseStatements {

	public static final String CREATE_OFFERS_TABLE_STATEMENT = """
				CREATE TABLE IF NOT EXISTS "offers" (
					"id" TEXT,
					"is_active": BOOLEAN,
					"is_generated": BOOLEAN,
					"seller_id" TEXT,
					"seller_name" TEXT,
					"stack" TEXT,
					"price" INTEGER,
					"duration": INTEGER,
					"time_posted_tick" INTEGER,
					"time_posted_datetime" TEXT,
					PRIMARY KEY (id)
				)
			""";

	public static final String CREATE_MAIL_TRANSIT_TABLE_STATEMENT = """
				CREATE TABLE IF NOT EXISTS "mail_transit" (
					"id" TEXT,
					"recipient_id" TEXT,
					"recipient_name" TEXT,
					"stack" TEXT,
					"time_dispatched_tick" INTEGER,
					"time_dispatched_datetime" TEXT,
					"time_last_delivery_attempted_tick" INTEGER,
					"time_last_delivery_attempted_datetime" TEXT,
					"number_of_delivery_attempts" INTEGER,
					PRIMARY KEY (id)
				)
			""";

	public static final String CREATE_TRANSACTIONS_TABLE_STATEMENT = """
				CREATE TABLE IF NOT EXISTS "transactions" (
					"id" TEXT,
					"seller_id" TEXT,
					"seller_name" TEXT,
					"buyer_id" TEXT,
					"buyer_name" TEXT,
					"stacks" TEXT,
					"price" INTEGER,
					"is_generated" BOOLEAN,
					"time_purchased_tick" INTEGER,
					"time_purchased_datetime" TEXT,
					"time_posted_tick" INTEGER,
					"time_posted_datetime" TEXT,
					PRIMARY KEY (id)
				)
			""";

}
