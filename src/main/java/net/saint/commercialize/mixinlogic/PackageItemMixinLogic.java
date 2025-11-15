package net.saint.commercialize.mixinlogic;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mrcrayfish.furniture.refurbished.client.util.ScreenHelper;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public interface PackageItemMixinLogic {

	// Configuration

	public static final String HOST_MOD_ID = "refurbished_furniture";

	public static final Identifier PACKAGE_ITEM_ID = new Identifier(HOST_MOD_ID, "package");

	public static final String MESSAGE_NBT_KEY = "MessageText";
	public static final String SENDER_NBT_KEY = "SenderText";

	// Access

	default ItemStack commercialize$create(DefaultedList<ItemStack> items, Text message, Text sender) {
		var stack = new ItemStack(Registries.ITEM.get(PACKAGE_ITEM_ID));
		var nbt = stack.getOrCreateNbt();

		Inventories.writeNbt(nbt, items);

		nbt.putString(MESSAGE_NBT_KEY, Text.Serializer.toJson(message));
		nbt.putString(SENDER_NBT_KEY, Text.Serializer.toJson(sender));

		return stack;
	}

	// Tooltip

	default void commercialize$appendTooltip(ItemStack stack, World world, List<Text> lines, TooltipContext context,
			CallbackInfo callbackInfo) {
		if (world == null || !commercialize$hasModNBT(stack)) {
			return;
		}

		commercialize$withTextFromNBT(stack, SENDER_NBT_KEY, senderText -> {
			lines.add(Text.translatable("gui." + HOST_MOD_ID + ".package_sent_by", senderText).formatted(Formatting.AQUA));
		});

		commercialize$withTextFromNBT(stack, MESSAGE_NBT_KEY, messageText -> {
			ScreenHelper.splitText(messageText.getString(), 170).forEach(component -> {
				lines.add(component.formatted(Formatting.GRAY));
			});
		});

		lines.add(Text.translatable("gui." + HOST_MOD_ID + ".package_open").formatted(Formatting.YELLOW));
		callbackInfo.cancel();
	}

	// Utility

	private static boolean commercialize$hasModNBT(ItemStack stack) {
		return stack.hasNbt() && stack.getNbt().contains(SENDER_NBT_KEY);
	}

	private static void commercialize$withTextFromNBT(ItemStack stack, String key, Consumer<Text> block) {
		if (!stack.hasNbt() || !stack.getNbt().contains(key)) {
			return;
		}

		var encoded = stack.getNbt().getString(key);
		var text = Text.Serializer.fromJson(encoded);

		if (text != null) {
			block.accept(text);
		}
	}

}
