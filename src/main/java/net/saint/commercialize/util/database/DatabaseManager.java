package net.saint.commercialize.util.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.saint.commercialize.Commercialize;

public class DatabaseManager {

	// Configuration

	/**
	 * The filename used for the database file stored in the world save's `data/` directory.
	 */
	private static final String DATABASE_FILE_NAME = Commercialize.MOD_ID + ".db";

	// State

	private Connection connection;

	private Path databasePath;

	// Init

	public DatabaseManager(MinecraftServer server) {
		this.databasePath = getDatabasePath(server);

		try {
			this.connect();
			this.setUpTables();
		} catch (Exception error) {
			Commercialize.LOGGER.error("Failed to load and initialize database at path: '" + this.databasePath + "'.", error);
		}
	}

	// Set-Up

	private void setUpTables() throws Exception {
		var statement = this.connection.createStatement();

		// Tables
		statement.execute(DatabaseSetUpStatements.CREATE_OFFERS_TABLE_STATEMENT);
		statement.execute(DatabaseSetUpStatements.CREATE_MAIL_TRANSIT_TABLE_STATEMENT);
		statement.execute(DatabaseSetUpStatements.CREATE_TRANSACTIONS_TABLE_STATEMENT);

		// Indices
		statement.execute(DatabaseSetUpStatements.CREATE_OFFERS_INDICES_STATEMENT);
		statement.execute(DatabaseSetUpStatements.CREATE_MAIL_TRANSIT_INDICES_STATEMENT);
		statement.execute(DatabaseSetUpStatements.CREATE_TRANSACTIONS_INDICES_STATEMENT);

		// Terminate
		statement.close();
		this.connection.commit();
	}

	// Tear-Down

	public void tearDown() {
		try {
			this.disconnect();
		} catch (Exception error) {
			Commercialize.LOGGER.error("Failed to close database connection at path: '" + this.databasePath + "'.", error);
		}
	}

	// Path

	private static Path getDatabasePath(MinecraftServer server) {
		var worldDirectory = server.getSavePath(WorldSavePath.ROOT);
		var dataDirectory = worldDirectory.resolve("data");

		return dataDirectory.resolve(DATABASE_FILE_NAME);
	}

	// Connection

	private void connect() throws Exception {
		var url = "jdbc:sqlite:" + this.databasePath.toString();
		var connection = DriverManager.getConnection(url);
		connection.setAutoCommit(false);

		this.connection = connection;
	}

	private void disconnect() throws Exception {
		this.connection.close();
		this.connection = null;
	}
}
