package net.saint.commercialize.util;

import java.io.IOException;
import java.nio.file.Files;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.saint.commercialize.Commercialize;

public final class ConfigDefaultsUtil {

	public static void copyDefaultConfigsIfNotPresent(ResourceManager resourceManager) {
		copyDirectoryIfNotPresent(resourceManager, "defaults/items", "items");
		copyDirectoryIfNotPresent(resourceManager, "defaults/offers", "offers");
		copyDirectoryIfNotPresent(resourceManager, "defaults/players", "players");
	}

	private static void copyDirectoryIfNotPresent(ResourceManager resourceManager, String dataSubdirectory,
			String configSubdirectory) {
		var configDir = FabricLoader.getInstance().getConfigDir().resolve(Commercialize.MOD_ID);
		var configRoot = configDir.resolve(configSubdirectory);

		if (Files.exists(configRoot)) {
			return;
		}

		copyDirectory(resourceManager, dataSubdirectory, configSubdirectory);
		Commercialize.LOGGER.info("Copied default config files to directory '{}'.", configRoot);
	}

	private static void copyDirectory(ResourceManager resourceManager, String dataSubdirectory, String configSubdirectory) {
		var foundResources = resourceManager.findResources(dataSubdirectory, identifier -> {
			return identifier.getNamespace().equals(Commercialize.MOD_ID) && identifier.getPath().endsWith(".json");
		});

		var configDir = FabricLoader.getInstance().getConfigDir().resolve(Commercialize.MOD_ID);
		var configRoot = configDir.resolve(configSubdirectory);

		for (var entry : foundResources.entrySet()) {
			var identifier = entry.getKey(); // e.g. "commercialize:defaults/items/items_food.json"

			// Strip the dataSubdirectory prefix from the path
			var path = identifier.getPath(); // "defaults/items/items_food.json"
			var pathPrefix = dataSubdirectory.endsWith("/") ? dataSubdirectory : (dataSubdirectory + "/");
			var relativePath = path.startsWith(pathPrefix) ? path.substring(pathPrefix.length()) : path;
			var outputPath = configRoot.resolve(relativePath);
			var resource = entry.getValue();

			try (var inputStream = resource.getInputStream()) {
				Files.createDirectories(outputPath.getParent());
				Files.copy(inputStream, outputPath);
			} catch (IOException exception) {
				Commercialize.LOGGER.error(
						"Could not copy default config '{}' from data resources to path '{}'. {}",
						identifier,
						outputPath,
						exception.getMessage()
				);
			}
		}
	}
}
