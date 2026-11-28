package pl.adixuyt.kitauto.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class BeetrootSoupCooldownMixin {
    @Unique
    private static final long KIT_AUTO_USE_COOLDOWN_NANOS = 1_500_000_000L;

    @Unique
    private static long kitAuto$lastBeetrootSoupPacketNanos = 0L;

    @Unique
    private static long kitAuto$lastFireChargePacketNanos = 0L;

    /**
     * Filters the outgoing interaction packet itself, before it reaches the connection.
     * Soup is used through PlayerInteractItem; Fire Charges can be used both in air
     * and on a block, so both packet types are checked for Fire Charges.
     */
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void kitAuto$filterRapidRightClickPackets(Packet<?> packet, CallbackInfo ci) {
        Hand hand;
        boolean blockInteraction;

        if (packet instanceof PlayerInteractItemC2SPacket itemPacket) {
            hand = itemPacket.getHand();
            blockInteraction = false;
        } else if (packet instanceof PlayerInteractBlockC2SPacket blockPacket) {
            hand = blockPacket.getHand();
            blockInteraction = true;
        } else {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        ItemStack heldStack = client.player.getStackInHand(hand);
        long now = System.nanoTime();

        if (heldStack.isOf(Items.BEETROOT_SOUP)) {
            // A block-click packet can be sent before the game falls back to item use.
            // Only count the actual item-use packet for soup.
            if (blockInteraction) {
                return;
            }

            if (kitAuto$lastBeetrootSoupPacketNanos != 0L
                    && now - kitAuto$lastBeetrootSoupPacketNanos < KIT_AUTO_USE_COOLDOWN_NANOS) {
                ci.cancel();
                return;
            }

            kitAuto$lastBeetrootSoupPacketNanos = now;
            return;
        }

        if (heldStack.isOf(Items.FIRE_CHARGE)) {
            if (kitAuto$lastFireChargePacketNanos != 0L
                    && now - kitAuto$lastFireChargePacketNanos < KIT_AUTO_USE_COOLDOWN_NANOS) {
                ci.cancel();
                return;
            }

            kitAuto$lastFireChargePacketNanos = now;
        }
    }
}
