package pl.adixuyt.kitauto;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/**
 * Starts a one-second cooldown only when soup or a fire charge was actually
 * used, or when the server removes/reduces the held stack after an interaction.
 * Inventory/chest transfers are ignored because they do not create a pending
 * use attempt, and pending attempts are discarded when a screen is open.
 */
public final class UseCooldownState {
    private static final long COOLDOWN_NANOS = 1_000_000_000L;
    private static final long PENDING_TIMEOUT_NANOS = 2_000_000_000L;

    private static long lastSoupUseNanos;
    private static long lastFireChargeUseNanos;

    private static PendingUse soupPending;
    private static PendingUse fireChargePending;

    private UseCooldownState() {
    }

    /**
     * Called at the beginning of an item interaction. Returns true if this item
     * is still cooling down; otherwise remembers the stack to detect a later
     * server-side decrease/removal.
     */
    public static boolean shouldBlockUse(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        long now = System.nanoTime();

        if (stack.isOf(Items.BEETROOT_SOUP)) {
            if (isCoolingDown(lastSoupUseNanos, now)) {
                return true;
            }
            if (soupPending == null) {
                soupPending = new PendingUse(hand, item, stack.getCount(),
                        selectedSlot(player, hand), now);
            }
            return false;
        }

        if (stack.isOf(Items.FIRE_CHARGE)) {
            if (isCoolingDown(lastFireChargeUseNanos, now)) {
                return true;
            }
            if (fireChargePending == null) {
                fireChargePending = new PendingUse(hand, item, stack.getCount(),
                        selectedSlot(player, hand), now);
            }
        }

        return false;
    }

    /**
     * Starts cooldown after Minecraft reports an accepted interaction.
     */
    public static void finishUse(Hand hand, ActionResult result) {
        if (result == null || !result.isAccepted()) {
            return;
        }

        long now = System.nanoTime();
        if (soupPending != null && soupPending.hand == hand) {
            lastSoupUseNanos = now;
            soupPending = null;
        }
        if (fireChargePending != null && fireChargePending.hand == hand) {
            lastFireChargeUseNanos = now;
            fireChargePending = null;
        }
    }

    /**
     * Detects server-side stack consumption/removal after a use attempt.
     * This is intentionally disabled while any inventory/container screen is open.
     */
    public static void observeInventory(MinecraftClient client) {
        if (client == null || client.player == null) {
            soupPending = null;
            fireChargePending = null;
            return;
        }

        if (client.currentScreen != null) {
            soupPending = null;
            fireChargePending = null;
            return;
        }

        long now = System.nanoTime();
        soupPending = observeOne(client.player, soupPending, now, true);
        fireChargePending = observeOne(client.player, fireChargePending, now, false);
    }

    private static PendingUse observeOne(PlayerEntity player, PendingUse pending,
                                         long now, boolean soup) {
        if (pending == null) {
            return null;
        }

        if (pending.hand == Hand.MAIN_HAND
                && player.getInventory().selectedSlot != pending.selectedSlot) {
            return null;
        }

        ItemStack current = player.getStackInHand(pending.hand);
        boolean disappearedOrReduced = current.isEmpty()
                || current.getItem() != pending.item
                || current.getCount() < pending.initialCount;

        if (disappearedOrReduced) {
            if (soup) {
                lastSoupUseNanos = now;
            } else {
                lastFireChargeUseNanos = now;
            }
            return null;
        }

        if (now - pending.startedNanos > PENDING_TIMEOUT_NANOS) {
            return null;
        }

        return pending;
    }

    private static int selectedSlot(PlayerEntity player, Hand hand) {
        return hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : -1;
    }

    private static boolean isCoolingDown(long lastUseNanos, long now) {
        return lastUseNanos != 0L && now - lastUseNanos < COOLDOWN_NANOS;
    }

    private static final class PendingUse {
        private final Hand hand;
        private final Item item;
        private final int initialCount;
        private final int selectedSlot;
        private final long startedNanos;

        private PendingUse(Hand hand, Item item, int initialCount,
                           int selectedSlot, long startedNanos) {
            this.hand = hand;
            this.item = item;
            this.initialCount = initialCount;
            this.selectedSlot = selectedSlot;
            this.startedNanos = startedNanos;
        }
    }
}
