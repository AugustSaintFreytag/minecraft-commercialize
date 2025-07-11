package net.saint.commercialize.data.market;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
import net.saint.commercialize.data.offer.Offer;

public final class MarketOfferCollection extends PersistentState {

	// Properties

	private List<Offer> offers = new ArrayList<Offer>();
	private Map<UUID, Offer> offersById = new HashMap<>();
	private boolean offersAreCapped = false;

	// Access

	public Stream<Offer> getOffers() {
		return offers.stream();
	}

	public Optional<Offer> getOffer(UUID id) {
		return Optional.of(offersById.get(id));
	}

	public boolean hasOffer(UUID id) {
		return offersById.containsKey(id);
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
		offersById.put(offer.id, offer);
		markDirty();
	}

	public void removeOffer(Offer offer) {
		offers.removeIf(element -> offer.id.equals(element.id));
		offersById.remove(offer.id);
		markDirty();
	}

	public void removeOffer(UUID id) {
		var offer = offersById.get(id);

		if (offer == null) {
			return;
		}

		offers.remove(offer);
		offersById.remove(id);

		markDirty();
	}

	public void addOffers(Collection<Offer> offers) {
		offers.forEach(offer -> this.addOffer(offer));
	}

	public void clearOffers() {
		offers.clear();
		offersById.clear();

		markDirty();
	}

	public void setOffersAreCapped(boolean capped) {
		this.offersAreCapped = capped;
		markDirty();
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

	public static MarketOfferCollection fromNbt(NbtCompound nbt) {
		var collection = new MarketOfferCollection();

		var list = nbt.getList("offers", 10); // 10 = NbtCompound

		for (int i = 0; i < list.size(); i++) {
			var offerNbt = list.getCompound(i);
			collection.addOffer(Offer.fromNBT(offerNbt));
		}

		return collection;
	}

}
