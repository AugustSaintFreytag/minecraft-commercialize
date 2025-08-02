package net.saint.commercialize.data.mail;

import java.util.ArrayList;
import java.util.List;
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

	private List<MailTransitItem> items = new ArrayList<>();

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

		for (MailTransitItem item : items) {
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

		for (int i = 0; i < list.size(); i++) {
			var itemNbt = list.getCompound(i);
			manager.items.add(MailTransitItem.fromNbt(itemNbt));
		}

		return manager;
	}

	// Access

	public Stream<MailTransitItem> getItems() {
		return items.stream();
	}

	public void pushItem(MailTransitItem item) {
		items.add(item);
		markDirty();
	}

	public void removeItem(MailTransitItem item) {
		items.remove(item);
		markDirty();
	}

}
