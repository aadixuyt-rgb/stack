package pl.adixuyt.kitauto;

import net.minecraft.client.MinecraftClient;

/**
 * Holds the independent cooldown and press-lock state for consumable items.
 * This is a regular helper class rather than a Mixin, so its public methods
 * are safe to call from the client tick handler.
 */
public final class UseCooldownState {
    private static final long SOUP_COOLDOWN_NANOS = 1_500_000_000L;
    private static final long FIRE_CHARGE_COOLDOWN_NANOS = 1_000_000_000L;

    private static long lastSoupUseNanos;
    private static long lastFireChargeUseNanos;
    private static boolean soupBlockedUntilRelease;
    private static boolean fireChargeBlockedUntilRelease;

    private UseCooldownState() {
    }

    /**
     * Unlocks each item's press lock only after the physical use key is released.
     * The independent time cooldowns are not reset here.
     */
    public static void updateReleaseState(MinecraftClient client) {
        if (client == null || client.options == null || client.options.useKey.isPressed()) {
            return;
        }

        soupBlockedUntilRelease = false;
        fireChargeBlockedUntilRelease = false;
    }

    public static boolean shouldBlockSoupUse() {
        long now = System.nanoTime();
        if (soupBlockedUntilRelease
                || (lastSoupUseNanos != 0L && now - lastSoupUseNanos < SOUP_COOLDOWN_NANOS)) {
            soupBlockedUntilRelease = true;
            return true;
        }

        lastSoupUseNanos = now;
        soupBlockedUntilRelease = true;
        return false;
    }

    public static boolean shouldBlockFireChargeUse() {
        long now = System.nanoTime();
        if (fireChargeBlockedUntilRelease
                || (lastFireChargeUseNanos != 0L
                && now - lastFireChargeUseNanos < FIRE_CHARGE_COOLDOWN_NANOS)) {
            fireChargeBlockedUntilRelease = true;
            return true;
        }

        lastFireChargeUseNanos = now;
        fireChargeBlockedUntilRelease = true;
        return false;
    }
}
