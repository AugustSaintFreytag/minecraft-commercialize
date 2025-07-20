package net.saint.commercialize.block.shipping;

import io.wispforest.owo.util.ImplementedInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.init.ModBlocks;
import net.saint.commercialize.screen.shipping.ShippingBlockScreenHandler;

public class ShippingBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_block_entity");

	public static final String INVENTORY_NBT_KEY = "items";

	// Properties

	private final ShippingBlockInventory inventory = new ShippingBlockInventory();

	// Init

	public ShippingBlockEntity(BlockPos position, BlockState state) {
		super(ModBlocks.SHIPPING_BLOCK_ENTITY, position, state);

		inventory.addListener(inventory -> {
			this.markDirty();
		});
	}

	// NBT

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);

		if (!nbt.contains(INVENTORY_NBT_KEY)) {
			return;
		}

		var inventoryNbtCompound = nbt.getCompound(INVENTORY_NBT_KEY);
		this.inventory.readNbtCompound(inventoryNbtCompound);
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		var inventoryNbtCompound = this.inventory.toNbtCompound();
		nbt.put(INVENTORY_NBT_KEY, inventoryNbtCompound);

		super.writeNbt(nbt);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		var nbt = new NbtCompound();
		writeNbt(nbt);

		return nbt;
	}

	// Inventory

	@Override
	public DefaultedList<ItemStack> getItems() {
		return this.inventory.getStacks();
	}

	// Tick

	public static void tick(World world, BlockPos position, BlockState state, ShippingBlockEntity blockEntity) {
	}

	// Screen

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new ShippingBlockScreenHandler(syncId, playerInventory, this.inventory);
	}

	@Override
	public Text getDisplayName() {
		return Text.of("Shipping Block");
	}

}
