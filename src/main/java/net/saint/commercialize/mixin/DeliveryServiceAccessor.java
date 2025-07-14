package net.saint.commercialize.mixin;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mrcrayfish.furniture.refurbished.mail.DeliveryService;
import com.mrcrayfish.furniture.refurbished.mail.Mailbox;

@Mixin(DeliveryService.class)
public interface DeliveryServiceAccessor {

	@Accessor(value = "mailboxes", remap = false)
	Map<UUID, Mailbox> getMailboxes();

}