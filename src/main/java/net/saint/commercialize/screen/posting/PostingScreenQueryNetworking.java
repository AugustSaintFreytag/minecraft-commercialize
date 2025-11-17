package net.saint.commercialize.screen.posting;

public final class PostingScreenQueryNetworking {

	public record C2SBalanceRequestMessage() {
	}

	public record S2CBalanceResponseMessage(int balance) {
	}

}
