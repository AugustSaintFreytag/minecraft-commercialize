package net.saint.commercialize.block.posting;

import io.wispforest.owo.util.ImplementedInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.saint.commercialize.Commercialize;
import net.saint.commercialize.init.ModBlockEntities;
import net.saint.commercialize.screen.posting.PostingScreenHandler;
import net.saint.commercialize.screen.posting.PostingScreenState;
import net.saint.commercialize.util.localization.LocalizationUtil;

public class PostingBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {

	// Configuration

	public static final Identifier ID = new Identifier(Commercialize.MOD_ID, "posting_block_entity");

	public static final String INVENTORY_NBT_KEY = "inventory";
	public static final String STATE_NBT_KEY = "posting_screen_state";

	// Properties

	private final SimpleInventory inventory = new SimpleInventory(1);

	private PostingScreenState state = new PostingScreenState();

	// Init

	public PostingBlockEntity(BlockPos position, BlockState state) {
		super(ModBlockEntities.POSTING_BLOCK_ENTITY, position, state);
	}

	// NBT

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);

		if (nbt.contains(INVENTORY_NBT_KEY)) {
			var inventoryNbtList = nbt.getList(INVENTORY_NBT_KEY, 10);
			var itemStackNbtCompound = inventoryNbtList.getCompound(0);

			this.inventory.setStack(0, ItemStack.fromNbt(itemStackNbtCompound));
		}

		if (nbt.contains(STATE_NBT_KEY)) {
			var stateNbtCompound = nbt.getCompound(STATE_NBT_KEY);
			this.state.readNbtCompound(stateNbtCompound);
		}
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		var inventoryNbtList = new NbtList();
		var itemStackNbtCompound = this.inventory.getStack(0).writeNbt(new NbtCompound());
		inventoryNbtList.add(itemStackNbtCompound);
		nbt.put(INVENTORY_NBT_KEY, inventoryNbtList);

		var stateNbtCompound = this.state.toNbtCompound();
		nbt.put(STATE_NBT_KEY, stateNbtCompound);

		super.writeNbt(nbt);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		var nbt = new NbtCompound();
		writeNbt(nbt);

		return nbt;
	}

	// Access

	public PostingScreenState getScreenState() {
		return this.state;
	}

	public void setScreenState(PostingScreenState state) {
		this.state = state;
		this.markDirty();
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		var stacks = DefaultedList.ofSize(1, ItemStack.EMPTY);
		stacks.set(0, this.inventory.getStack(0));

		return stacks;
	}

	// Tick

	public static void tick(World world, BlockPos position, BlockState state, PostingBlockEntity blockEntity) {
		// No action but required for block entity to be retained.
	}

	// Screen

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new PostingScreenHandler(syncId, this, playerInventory, this.inventory);
	}

	@Override
	public Text getDisplayName() {
		return LocalizationUtil.localizedText("block", "posting_block");
	}

}
