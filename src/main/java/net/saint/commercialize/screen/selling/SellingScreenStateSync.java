package net.saint.commercialize.screen.selling;

import net.minecraft.item.ItemStack;

public final class SellingScreenStateSync {

	private interface StateSyncMessage {
		ItemStack selectedItem();

		int offerPrice();

		long offerDuration();

		OfferPostStrategy offerPostStrategy();
	}

	public record C2SStateSyncMessage(ItemStack selectedItem, int offerPrice, long offerDuration, OfferPostStrategy offerPostStrategy)
			implements StateSyncMessage {
	}

	public record S2CStateSyncMessage(ItemStack selectedItem, int offerPrice, long offerDuration, OfferPostStrategy offerPostStrategy)
			implements StateSyncMessage {
	}

	public record C2SStateRequestMessage() {
	}

	public static SellingScreenState stateFromMessage(StateSyncMessage message) {
		var state = new SellingScreenState();

		state.selectedItem = message.selectedItem();
		state.offerPrice = message.offerPrice();
		state.offerDuration = message.offerDuration();
		state.offerPostStrategy = message.offerPostStrategy();

		return state;
	}

	public static S2CStateSyncMessage clientboundMessageFromState(SellingScreenState state) {
		return new S2CStateSyncMessage(state.selectedItem, state.offerPrice, state.offerDuration, state.offerPostStrategy);
	}

	public static C2SStateSyncMessage serverboundMessageFromState(SellingScreenState state) {
		return new C2SStateSyncMessage(state.selectedItem, state.offerPrice, state.offerDuration, state.offerPostStrategy);
	}

	public static void overrideStateFromMessage(SellingScreenState state, StateSyncMessage message) {
		state.selectedItem = message.selectedItem();
		state.offerPrice = message.offerPrice();
		state.offerDuration = message.offerDuration();
		state.offerPostStrategy = message.offerPostStrategy();
	}

}
