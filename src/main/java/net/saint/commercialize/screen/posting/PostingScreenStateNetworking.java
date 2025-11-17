package net.saint.commercialize.screen.posting;

import net.minecraft.item.ItemStack;

public final class PostingScreenStateNetworking {

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

	public static PostingScreenState stateFromMessage(StateSyncMessage message) {
		var state = new PostingScreenState();

		state.stack = message.selectedItem();
		state.price = message.offerPrice();
		state.duration = message.offerDuration();
		state.postStrategy = message.offerPostStrategy();

		return state;
	}

	public static S2CStateSyncMessage clientboundStateSyncMessageFromState(PostingScreenState state) {
		return new S2CStateSyncMessage(state.stack, state.price, state.duration, state.postStrategy);
	}

	public static C2SStateSyncMessage serverboundStateSyncMessageFromState(PostingScreenState state) {
		return new C2SStateSyncMessage(state.stack, state.price, state.duration, state.postStrategy);
	}

	public static void overrideStateFromMessage(PostingScreenState state, StateSyncMessage message) {
		state.stack = message.selectedItem();
		state.price = message.offerPrice();
		state.duration = message.offerDuration();
		state.postStrategy = message.offerPostStrategy();
	}

}
