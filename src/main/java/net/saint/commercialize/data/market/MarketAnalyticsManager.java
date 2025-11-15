package net.saint.commercialize.data.market;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.saint.commercialize.Commercialize;

public final class MarketAnalyticsManager extends PersistentState {

	// Configuration

	public static final String NAME = Commercialize.MOD_ID + "_analytics";

	// Events

	@FunctionalInterface
	public interface StateModified {
		void onStateModified(MarketAnalyticsManager manager);
	}

	public final Event<StateModified> STATE_MODIFIED = EventFactory.createArrayBacked(StateModified.class, listeners -> manager -> {
		for (var listener : listeners) {
			listener.onStateModified(manager);
		}
	});

	// Properties

	private Map<UUID, MarketPlayerReport> reportsByPlayerId = new HashMap<>();

	// Load

	public static MarketAnalyticsManager loadFromServer(MinecraftServer server) {
		var persistentStateManager = server.getOverworld().getPersistentStateManager();
		var state = persistentStateManager.getOrCreate(MarketAnalyticsManager::fromNbt, MarketAnalyticsManager::new, NAME);

		return state;
	}

	// NBT

	public NbtCompound writeNbt(NbtCompound nbt) {
		var reportsNbt = new NbtCompound();

		for (var entry : reportsByPlayerId.entrySet()) {
			var playerId = entry.getKey();
			var report = entry.getValue();

			var reportNbt = new NbtCompound();
			report.writeNbt(reportNbt);

			reportsNbt.put(playerId.toString(), reportNbt);
		}

		nbt.put("reports", reportsNbt);
		return nbt;
	}

	public static MarketAnalyticsManager fromNbt(NbtCompound nbt) {
		var manager = new MarketAnalyticsManager();
		var reportsNbt = nbt.getCompound("reports");

		for (String key : reportsNbt.getKeys()) {
			var playerId = UUID.fromString(key);
			var reportNbt = reportsNbt.getCompound(key);
			var report = MarketPlayerReport.fromNbt(reportNbt);

			manager.reportsByPlayerId.put(playerId, report);
		}

		return manager;
	}

	// Invalidation

	@Override
	public void markDirty() {
		super.markDirty();
		STATE_MODIFIED.invoker().onStateModified(this);
	}

	// Access

	public MarketPlayerReport getReportForPlayerId(UUID playerId) {
		return reportsByPlayerId.get(playerId);
	}

	public MarketPlayerReport getOrCreateReportForProfile(GameProfile profile) {
		return reportsByPlayerId.computeIfAbsent(profile.getId(), id -> {
			var report = new MarketPlayerReport();

			report.playerId = profile.getId();
			report.playerName = profile.getName();

			return report;
		});
	}

	public void addReport(MarketPlayerReport report) {
		reportsByPlayerId.put(report.playerId, report);
		markDirty();
	}

}
