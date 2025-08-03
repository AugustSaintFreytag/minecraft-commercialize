package net.saint.commercialize.data.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.saint.commercialize.Commercialize;

public final class MailTransitManager extends PersistentState {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "mail_transit_manager");

	// Properties

	private final AtomicReference<List<MailTransitItem>> items = new AtomicReference<>(Collections.emptyList());

	// Load

	public static MailTransitManager loadFromServer(MinecraftServer server) {
		var persistentStateManager = server.getOverworld().getPersistentStateManager();
		var state = persistentStateManager.getOrCreate(MailTransitManager::fromNbt, MailTransitManager::new, ID.toString());

		return state;
	}

	// NBT

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		var list = new NbtList();

		for (MailTransitItem item : items.get()) {
			var itemNbt = new NbtCompound();
			item.writeNbt(itemNbt);
			list.add(itemNbt);
		}

		nbt.put("items", list);
		return nbt;
	}

	public static MailTransitManager fromNbt(NbtCompound nbt) {
		var manager = new MailTransitManager();

		var list = nbt.getList("items", 10); // 10 = NbtCompound
		var newItems = new ArrayList<MailTransitItem>();

		for (int i = 0; i < list.size(); i++) {
			var itemNbt = list.getCompound(i);
			newItems.add(MailTransitItem.fromNbt(itemNbt));
		}

		manager.items.set(Collections.unmodifiableList(newItems));
		return manager;
	}

	// Access

	public Stream<MailTransitItem> getItems() {
		return items.get().stream();
	}

	public void pushItem(MailTransitItem item) {
		items.updateAndGet(currentList -> {
			var newList = new ArrayList<>(currentList);
			newList.add(item);
			return Collections.unmodifiableList(newList);
		});

		markDirty();
	}

	public void removeItem(MailTransitItem item) {
		items.updateAndGet(currentList -> {
			var newList = new ArrayList<>(currentList);
			newList.remove(item);
			return Collections.unmodifiableList(newList);
		});

		markDirty();
	}

	public void clearItems() {
		items.set(Collections.emptyList());
		markDirty();
	}

}
