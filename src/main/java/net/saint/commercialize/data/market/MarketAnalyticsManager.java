package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	private static final String INTERVAL_REPORTS_NBT_KEY = "IntervalReports";
	private static final String ALL_TIME_REPORTS_NBT_KEY = "AllTimeReports";

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

	// State

	private Map<UUID, MarketPlayerReport> intervalReportsByPlayerId = new HashMap<>();
	private Map<UUID, MarketPlayerReport> allTimeReportsByPlayerId = new HashMap<>();

	// Load

	public static MarketAnalyticsManager loadFromServer(MinecraftServer server) {
		var persistentStateManager = server.getOverworld().getPersistentStateManager();
		var state = persistentStateManager.getOrCreate(MarketAnalyticsManager::fromNbt, MarketAnalyticsManager::new, NAME);

		return state;
	}

	// NBT

	public NbtCompound writeNbt(NbtCompound nbt) {
		var intervalReportsNbt = encodeMapToNBT(intervalReportsByPlayerId);
		var allTimeReportsNbt = encodeMapToNBT(allTimeReportsByPlayerId);

		nbt.put(INTERVAL_REPORTS_NBT_KEY, intervalReportsNbt);
		nbt.put(ALL_TIME_REPORTS_NBT_KEY, allTimeReportsNbt);

		return nbt;
	}

	public static MarketAnalyticsManager fromNbt(NbtCompound nbt) {
		var manager = new MarketAnalyticsManager();
		var intervalReports = decodeMapFromNBT(nbt.getCompound(INTERVAL_REPORTS_NBT_KEY));
		var allTimeReports = decodeMapFromNBT(nbt.getCompound(ALL_TIME_REPORTS_NBT_KEY));

		manager.intervalReportsByPlayerId = intervalReports;
		manager.allTimeReportsByPlayerId = allTimeReports;

		return manager;
	}

	private static NbtCompound encodeMapToNBT(Map<UUID, MarketPlayerReport> map) {
		var nbt = new NbtCompound();

		for (var entry : map.entrySet()) {
			var playerId = entry.getKey();
			var report = entry.getValue();

			var reportNbt = new NbtCompound();
			report.writeNbt(reportNbt);

			nbt.put(playerId.toString(), reportNbt);
		}

		return nbt;
	}

	private static Map<UUID, MarketPlayerReport> decodeMapFromNBT(NbtCompound nbt) {
		var map = new HashMap<UUID, MarketPlayerReport>();

		for (String key : nbt.getKeys()) {
			var playerId = UUID.fromString(key);
			var reportNbt = nbt.getCompound(key);
			var report = MarketPlayerReport.fromNbt(reportNbt);

			map.put(playerId, report);
		}

		return map;
	}

	// Invalidation

	@Override
	public void markDirty() {
		super.markDirty();
		STATE_MODIFIED.invoker().onStateModified(this);
	}

	// Access (Interval)

	public List<MarketPlayerReport> getAllIntervalReports() {
		return new ArrayList<>(intervalReportsByPlayerId.values());
	}

	public MarketPlayerReport getIntervalReportForPlayerId(UUID playerId) {
		return intervalReportsByPlayerId.get(playerId);
	}

	public MarketPlayerReport getOrCreateIntervalReportForProfile(GameProfile profile) {
		return intervalReportsByPlayerId.computeIfAbsent(profile.getId(), id -> {
			var report = new MarketPlayerReport();

			report.playerId = profile.getId();
			report.playerName = profile.getName();

			return report;
		});
	}

	// Access (All-Time)

	public List<MarketPlayerReport> getAllAllTimeReports() {
		return new ArrayList<>(allTimeReportsByPlayerId.values());
	}

	public MarketPlayerReport getAllTimeReportForPlayerId(UUID playerId) {
		return allTimeReportsByPlayerId.get(playerId);
	}

	public MarketPlayerReport getOrCreateAllTimeReportForProfile(GameProfile profile) {
		return allTimeReportsByPlayerId.computeIfAbsent(profile.getId(), id -> {
			var report = new MarketPlayerReport();

			report.playerId = profile.getId();
			report.playerName = profile.getName();

			return report;
		});
	}

	// Mutation

	public void sealBuyerSideIntervalReportForPlayerId(GameProfile profile) {
		var intervalReport = getOrCreateIntervalReportForProfile(profile);
		var allTimeReport = getOrCreateAllTimeReportForProfile(profile);

		allTimeReport.unionBuyerSide(intervalReport);
		intervalReport.clearBuyerSide();

		markDirty();
	}

	public void sealSellerSideIntervalReportForPlayerId(GameProfile profile) {
		var intervalReport = getOrCreateIntervalReportForProfile(profile);
		var allTimeReport = getOrCreateAllTimeReportForProfile(profile);

		allTimeReport.unionSellerSide(intervalReport);
		intervalReport.clearSellerSide();

		markDirty();
	}

}
