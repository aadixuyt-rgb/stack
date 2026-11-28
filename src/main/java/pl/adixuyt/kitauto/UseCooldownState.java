package pl.adixuyt.kitauto;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/**
 * Applies a one-second cooldown from the moment the player attempts to use
 * beetroot soup or a fire charge. The interaction result does not matter,
 * so attempts made while aiming at the sky are covered as well.
 */
public final class UseCooldownState {
    private static final long COOLDOWN_NANOS = 1_000_000_000L;

    private static long lastSoupAttemptNanos;
    private static long lastFireChargeAttemptNanos;

    private UseCooldownState() {
    }

    /**
     * Returns true when the held item is cooling down. Otherwise starts its
     * cooldown immediately, at the beginning of the interaction attempt.
     */
    public static boolean shouldBlockUse(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        long now = System.nanoTime();

        if (stack.isOf(Items.BEETROOT_SOUP)) {
            if (isCoolingDown(lastSoupAttemptNanos, now)) {
                return true;
            }
            lastSoupAttemptNanos = now;
            return false;
        }

        if (stack.isOf(Items.FIRE_CHARGE)) {
            if (isCoolingDown(lastFireChargeAttemptNanos, now)) {
                return true;
            }
            lastFireChargeAttemptNanos = now;
            return false;
        }

        return false;
    }

    private static boolean isCoolingDown(long lastAttemptNanos, long now) {
        return lastAttemptNanos != 0L && now - lastAttemptNanos < COOLDOWN_NANOS;
    }

    // Kept for the existing mixin RETURN hook; cooldown starts at interaction HEAD.\n    public static void finishUse(Hand hand, ActionResult result) {\n        // Intentionally empty. The attempt itself starts the cooldown.\n    }\n\n    // Kept for the existing client tick hook; cooldown now starts at interaction HEAD.
    public static void observeInventory(MinecraftClient client) {
        // No inventory observation is needed: moving items in a GUI is not an interaction attempt.
    }
}
