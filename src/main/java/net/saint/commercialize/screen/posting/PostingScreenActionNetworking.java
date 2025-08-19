package net.saint.commercialize.screen.posting;

import net.minecraft.item.ItemStack;
import net.saint.commercialize.data.market.MarketOfferPostingUtil;
import net.saint.commercialize.data.market.MarketOfferPostingUtil.OfferDraft;

public final class PostingScreenActionNetworking {

	public record C2SClearOfferActionMessage() {
	}

	public record C2SPostOfferActionMessage(OfferDraft draft) {
	}

	public record S2CPostOfferActionMessage(MarketOfferPostingUtil.OfferPostingResult result, ItemStack stack) {
	}

}
