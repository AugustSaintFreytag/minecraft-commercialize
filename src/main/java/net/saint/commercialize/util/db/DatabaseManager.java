package net.saint.commercialize.util.db;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.saint.commercialize.Commercialize;

public class DatabaseManager {

	// Configuration

	/**
	 * The filename used for the database file stored in the world save's `data` directory.
	 */
	private static final String DATABASE_FILE_NAME = Commercialize.MOD_ID + ".db";

	// Executor

	private static final ExecutorService DATABASE_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
		var thread = new Thread(runnable, Commercialize.MOD_ID + "-database-thread");
		thread.setDaemon(true);

		return thread;
	});

	// State

	private final Jdbi jdbi;

	private Path databasePath;

	// Init

	public DatabaseManager(MinecraftServer server) {
		this.databasePath = getDatabasePath(server);

		var url = "jdbc:sqlite:" + this.databasePath;
		this.jdbi = Jdbi.create(url);

		try {
			this.setUpTables();
		} catch (Exception error) {
			Commercialize.LOGGER.error("Failed to load and initialize database at path: '" + this.databasePath + "'.", error);
		}
	}

	// Set-Up

	private void setUpTables() throws Exception {
		this.executeAsync(handle -> {
			// Tables
			handle.execute(DatabaseSetUpStatements.CREATE_OFFERS_TABLE_STATEMENT);
			handle.execute(DatabaseSetUpStatements.CREATE_TRANSIT_TABLE_STATEMENT);
			handle.execute(DatabaseSetUpStatements.CREATE_TRANSACTIONS_TABLE_STATEMENT);

			// Indices
			handle.execute(DatabaseSetUpStatements.CREATE_OFFERS_INDICES_STATEMENT);
			handle.execute(DatabaseSetUpStatements.CREATE_TRANSIT_INDICES_STATEMENT);
			handle.execute(DatabaseSetUpStatements.CREATE_TRANSACTIONS_INDICES_STATEMENT);

			// Commit
			handle.commit();
		}).exceptionally(error -> {
			Commercialize.LOGGER.error("Failed to set up database tables and indices.", error);
			return null;
		});
	}

	// Tear-Down

	public void tearDown() {
		// …
	}

	// Execution

	public <T> CompletableFuture<T> queryAsync(Function<Handle, T> task) {
		return CompletableFuture.supplyAsync(
				() -> jdbi.withHandle(handle -> task.apply(handle)),
				DATABASE_EXECUTOR
		);
	}

	public CompletableFuture<Void> executeAsync(Consumer<Handle> task) {
		return CompletableFuture.runAsync(
				() -> jdbi.useHandle(handle -> task.accept(handle)),
				DATABASE_EXECUTOR
		);
	}

	// Path

	private static Path getDatabasePath(MinecraftServer server) {
		var worldDirectory = server.getSavePath(WorldSavePath.ROOT);
		var dataDirectory = worldDirectory.resolve("data");

		return dataDirectory.resolve(DATABASE_FILE_NAME);
	}
}
