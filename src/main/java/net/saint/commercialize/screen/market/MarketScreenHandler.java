package net.saint.commercialize.screen.market;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class MarketScreenHandler extends ScreenHandler {

	protected MarketScreenHandler(ScreenHandlerType<?> type, int syncId) {
		super(type, syncId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'canUse'");
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'quickMove'");
	}

}