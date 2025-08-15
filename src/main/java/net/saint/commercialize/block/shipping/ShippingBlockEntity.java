package net.saint.commercialize.block.shipping;

import io.wispforest.owo.util.ImplementedInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.data.market.ShippingExchangeTickingUtil;
import net.saint.commercialize.init.ModBlockEntities;
import net.saint.commercialize.init.ModSounds;
import net.saint.commercialize.screen.selling.SellingScreenHandler;
import net.saint.commercialize.screen.shipping.ShippingScreenHandler;

public class ShippingBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "shipping_block_entity");

	public static final String INVENTORY_NBT_KEY = "items";

	// Properties

	public final ShippingBlockInventory inventory = new ShippingBlockInventory();

	private ShippingBlockViewMode viewMode = ShippingBlockViewMode.SELLING;

	// Init

	public ShippingBlockEntity(BlockPos position, BlockState state) {
		super(ModBlockEntities.SHIPPING_BLOCK_ENTITY, position, state);
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
		if (world.isClient()) {
			return;
		}

		ShippingExchangeTickingUtil.tickShippingIfNecessary(world, blockEntity, result -> {
			switch (result) {
				case SOLD:
					world.playSound(null, blockEntity.getPos(), ModSounds.MAILBOX_DELIVERY_SOUND, SoundCategory.BLOCKS, 1.5f, 0.75f);
					break;
				case NO_ITEMS:
					// No feedback sound when not selling items.
					// world.playSound(null, blockEntity.getPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 0.5f, 0.25f);
					break;
				case FAILURE:
					world.playSound(null, blockEntity.getPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 1f, 0.5f);
					break;
			}
		});
	}

	// Screen

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		switch (viewMode) {
			case SHIPPING:
				return new ShippingScreenHandler(syncId, getPos(), playerInventory, this.inventory);
			case SELLING:
				return new SellingScreenHandler(syncId, getPos(), playerInventory, new SimpleInventory(1));
			default:
				throw new IllegalStateException("Can not create menu with invalid view mode: " + viewMode + ".");
		}
	}

	@Override
	public Text getDisplayName() {
		return Text.of("Shipping Block");
	}

}
