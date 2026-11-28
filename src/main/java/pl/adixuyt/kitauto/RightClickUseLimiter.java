package pl.adixuyt.kitauto;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

/**
 * Limits repeat use packets for beetroot soup and fire charges.
 * One use is permitted per physical right-click hold, with a 1.5 second cooldown.
 */
public final class RightClickUseLimiter {
    private static final long COOLDOWN_NANOS = 1_500_000_000L;

    private static long lastSoupUseNanos;
    private static long lastFireChargeUseNanos;
    private static boolean soupBlockedUntilRelease;
    private static boolean fireChargeBlockedUntilRelease;

    private RightClickUseLimiter() {
    }

    /**
     * Called on each client tick so a fresh click is only enabled after the
     * right mouse button has actually been released.
     */
    public static void updateReleaseState(MinecraftClient client) {
        if (client == null || client.options == null || client.options.useKey.isPressed()) {
            return;
        }

        soupBlockedUntilRelease = false;
        fireChargeBlockedUntilRelease = false;
    }

    /**
     * Returns true when the outgoing interaction packet must be cancelled.
     */
    public static boolean shouldCancel(Packet<?> packet, MinecraftClient client) {
        Hand hand;
        boolean itemUsePacket;

        if (packet instanceof PlayerInteractItemC2SPacket itemPacket) {
            hand = itemPacket.getHand();
            itemUsePacket = true;
        } else if (packet instanceof PlayerInteractBlockC2SPacket blockPacket) {
            hand = blockPacket.getHand();
            itemUsePacket = false;
        } else {
            return false;
        }

        if (client == null || client.player == null) {
            return false;
        }

        ItemStack heldStack = client.player.getStackInHand(hand);
        long now = System.nanoTime();

        // Beetroot soup is consumed via item interaction, not the initial block click.
        if (itemUsePacket && heldStack.isOf(Items.BEETROOT_SOUP)) {
            if (soupBlockedUntilRelease
                    || (lastSoupUseNanos != 0L && now - lastSoupUseNanos < COOLDOWN_NANOS)) {
                soupBlockedUntilRelease = true;
                return true;
            }

            lastSoupUseNanos = now;
            soupBlockedUntilRelease = true;
            return false;
        }

        // Fire charges can be used either in the air or against a block.
        if (heldStack.isOf(Items.FIRE_CHARGE)) {
            if (fireChargeBlockedUntilRelease
                    || (lastFireChargeUseNanos != 0L
                    && now - lastFireChargeUseNanos < COOLDOWN_NANOS)) {
                fireChargeBlockedUntilRelease = true;
                return true;
            }

            lastFireChargeUseNanos = now;
            fireChargeBlockedUntilRelease = true;
        }

        return false;
    }
}
