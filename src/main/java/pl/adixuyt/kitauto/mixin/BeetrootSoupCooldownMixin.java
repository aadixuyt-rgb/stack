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
    private static long kitAuto$lastSoupUseNanos = 0L;

    @Unique
    private static long kitAuto$lastFireChargeUseNanos = 0L;

    @Unique
    private static boolean kitAuto$soupBlockedUntilRelease = false;

    @Unique
    private static boolean kitAuto$fireChargeBlockedUntilRelease = false;

    /**
     * Called once per client tick. A held right mouse button stays locked after
     * the first accepted use until the player physically releases it.
     */
    public static void updateReleaseState(MinecraftClient client) {
        if (client == null || client.options == null || client.options.useKey.isPressed()) {
            return;
        }

        kitAuto$soupBlockedUntilRelease = false;
        kitAuto$fireChargeBlockedUntilRelease = false;
    }

    /**
     * Filters outgoing right-click packets before they reach the server.
     * Soup is used through item interaction. Fire Charges can be used in air
     * or on a block, so both packet types are handled.
     */
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void kitAuto$filterRapidUsePackets(Packet<?> packet, CallbackInfo ci) {
        Hand hand;
        boolean itemUsePacket;

        if (packet instanceof PlayerInteractItemC2SPacket itemPacket) {
            hand = itemPacket.getHand();
            itemUsePacket = true;
        } else if (packet instanceof PlayerInteractBlockC2SPacket blockPacket) {
            hand = blockPacket.getHand();
            itemUsePacket = false;
        } else {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        ItemStack heldStack = client.player.getStackInHand(hand);
        long now = System.nanoTime();

        if (itemUsePacket && heldStack.isOf(Items.BEETROOT_SOUP)) {
            if (kitAuto$soupBlockedUntilRelease
                    || (kitAuto$lastSoupUseNanos != 0L
                    && now - kitAuto$lastSoupUseNanos < KIT_AUTO_USE_COOLDOWN_NANOS)) {
                kitAuto$soupBlockedUntilRelease = true;
                ci.cancel();
                return;
            }

            kitAuto$lastSoupUseNanos = now;
            kitAuto$soupBlockedUntilRelease = true;
            return;
        }

        if (heldStack.isOf(Items.FIRE_CHARGE)) {
            if (kitAuto$fireChargeBlockedUntilRelease
                    || (kitAuto$lastFireChargeUseNanos != 0L
                    && now - kitAuto$lastFireChargeUseNanos < KIT_AUTO_USE_COOLDOWN_NANOS)) {
                kitAuto$fireChargeBlockedUntilRelease = true;
                ci.cancel();
                return;
            }

            kitAuto$lastFireChargeUseNanos = now;
            kitAuto$fireChargeBlockedUntilRelease = true;
        }
    }
}
