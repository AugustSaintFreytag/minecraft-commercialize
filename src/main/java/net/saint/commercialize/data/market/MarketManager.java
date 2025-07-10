package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
import net.saint.commercialize.data.offer.Offer;

public final class MarketManager extends PersistentState {

	// Properties

	private List<Offer> offers = new ArrayList<Offer>();
	private boolean offersAreCapped = false;

	// Access

	public Stream<Offer> getOffers() {
		return offers.stream();
	}

	public Optional<Offer> getOffer(UUID id) {
		return offers.stream().filter(offer -> offer.id.equals(id)).findFirst();
	}

	public int size() {
		return offers.size();
	}

	public boolean isEmpty() {
		return offers.isEmpty();
	}

	public boolean offersAreCapped() {
		return offersAreCapped;
	}

	@Override
	public int hashCode() {
		return offers.stream().map(offer -> offer.id).toList().hashCode();
	}

	// Mutation

	public void addOffer(Offer offer) {
		offers.add(offer);
		markDirty();
	}

	public void removeOffer(Offer offer) {
		offers.remove(offer);
		markDirty();
	}

	public void removeOffer(UUID id) {
		offers.removeIf(offer -> offer.id.equals(id));
		markDirty();
	}

	public void removeOffers(Collection<Offer> offers) {
		this.offers.removeAll(offers);
		markDirty();
	}

	public void addOffers(Collection<Offer> offers) {
		this.offers.addAll(offers);
		markDirty();
	}

	public void clearOffers() {
		offers.clear();
		markDirty();
	}

	public void setOffersAreCapped(boolean capped) {
		this.offersAreCapped = capped;
	}

	// NBT

	public NbtCompound writeNbt(NbtCompound nbt) {
		var list = new net.minecraft.nbt.NbtList();

		for (Offer offer : offers) {
			var offerNbt = new net.minecraft.nbt.NbtCompound();
			offer.writeNbt(offerNbt);
			list.add(offerNbt);
		}

		nbt.put("offers", list);
		return nbt;
	}

	public static MarketManager fromNbt(NbtCompound nbt) {
		var manager = new MarketManager();

		var list = nbt.getList("offers", 10); // 10 = NbtCompound

		for (int i = 0; i < list.size(); i++) {
			var offerNbt = list.getCompound(i);
			manager.addOffer(Offer.fromNBT(offerNbt));
		}

		return manager;
	}

}
