package net.saint.commercialize.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.config.ItemsConfig;
import net.saint.commercialize.config.OffersConfig;
import net.saint.commercialize.config.PlayersConfig;
import net.saint.commercialize.data.common.IdentifierAdapter;

public final class ConfigLoadUtil {

	// Properties

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Identifier.class, new IdentifierAdapter())
			.create();

	private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(Commercialize.MOD_ID);

	// Load

	public static List<ItemsConfig> loadItemConfigs() {
		var itemConfigs = new ArrayList<ItemsConfig>();

		assertConfigDirectoriesAndFiles();
		forEachConfigFileInStream("items_*.json", (file, reader) -> {
			var rootObject = JsonParser.parseReader(reader).getAsJsonObject();

			if (!rootObject.has("values")) {
				Commercialize.LOGGER.warn("Could not read item config file '{}', invalid format.", file.getFileName());
				return;
			}

			ItemsConfig decodedConfig = GSON.fromJson(rootObject, ItemsConfig.class);
			itemConfigs.add(decodedConfig);
		});

		return itemConfigs;
	}

	public static List<OffersConfig> loadOfferTemplateConfigs() {
		var offerTemplateConfigs = new ArrayList<OffersConfig>();

		assertConfigDirectoriesAndFiles();
		forEachConfigFileInStream("offers_*.json", (file, reader) -> {
			var rootObject = JsonParser.parseReader(reader).getAsJsonObject();

			if (!rootObject.has("offers")) {
				Commercialize.LOGGER.warn("Could not read offer config file '{}', invalid format.", file.getFileName());
				return;
			}

			try {
				OffersConfig decodedConfig = GSON.fromJson(rootObject, OffersConfig.class);
				offerTemplateConfigs.add(decodedConfig);
			} catch (Exception e) {
				Commercialize.LOGGER.error("Could not decode offer config file '{}'", file.getFileName(), e);
			}
		});

		return offerTemplateConfigs;
	}

	public static PlayersConfig loadPlayersConfig() {
		assertConfigDirectoriesAndFiles();
		Path configFile = CONFIG_DIR.resolve("players.json");

		if (!Files.exists(configFile)) {
			Commercialize.LOGGER.warn("Players config file '{}' not found, using defaults.", configFile.getFileName());
			return new PlayersConfig();
		}

		try (var reader = Files.newBufferedReader(configFile)) {
			var root = JsonParser.parseReader(reader).getAsJsonObject();
			if (!root.has("players")) {
				Commercialize.LOGGER.warn("Could not read players config file '{}', invalid format.", configFile.getFileName());
				return new PlayersConfig();
			}
			return GSON.fromJson(root, PlayersConfig.class);
		} catch (IOException e) {
			Commercialize.LOGGER.error("Could not read players config file '{}'", configFile.getFileName(), e);
			return new PlayersConfig();
		} catch (Exception e) {
			Commercialize.LOGGER.error("Could not decode players config file '{}'", configFile.getFileName(), e);
			return new PlayersConfig();
		}
	}

	private static void forEachConfigFileInStream(String pattern, BiConsumer<Path, Reader> fileReaderConsumer) {
		try (var stream = Files.newDirectoryStream(CONFIG_DIR, pattern)) {
			for (var file : stream) {
				try (var reader = Files.newBufferedReader(file)) {
					fileReaderConsumer.accept(file, reader);
				} catch (IOException e) {
					Commercialize.LOGGER.error("Could not read config file '{}'", file.getFileName(), e);
				}
			}
		} catch (IOException e) {
			Commercialize.LOGGER.error("Could not list config files with pattern '{}'", pattern, e);
		}
	}

	// Assertions

	private static void assertConfigDirectoriesAndFiles() {
		try {
			Files.createDirectories(CONFIG_DIR);

			// ensure defaults exist
			copyIfMissing("items_any.json");
			copyIfMissing("offers_any.json");
		} catch (IOException e) {
			throw new RuntimeException("Could not create config directory or copy default config files.", e);
		}
	}

	private static void copyIfMissing(String resourceName) throws IOException {
		var target = CONFIG_DIR.resolve(resourceName);

		if (Files.exists(target)) {
			return;
		}

		try (InputStream in = ConfigLoadUtil.class.getResourceAsStream("/assets/commercialize/config/" + resourceName)) {
			Files.copy(in, target);
		} catch (IOException e) {
			Commercialize.LOGGER.error("Could not copy default config file '" + resourceName + "' to game config directory.", e);
		}
	}

}
