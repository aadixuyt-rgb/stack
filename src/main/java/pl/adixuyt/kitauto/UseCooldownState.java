package pl.adixuyt.kitauto;

import net.minecraft.client.MinecraftClient;

/**
 * Tracks the beetroot-soup use cooldown and right-click hold lock.
 */
public final class UseCooldownState {
    private static final long SOUP_COOLDOWN_NANOS = 1_500_000_000L;

    private static long lastSoupUseNanos;
    private static boolean soupBlockedUntilRelease;

    private UseCooldownState() {
    }

    /**
     * Unlocks a fresh soup use only after the physical use button is released.
     * The 1.5-second time cooldown remains active after release.
     */
    public static void updateReleaseState(MinecraftClient client) {
        if (client == null || client.options == null || client.options.useKey.isPressed()) {
            return;
        }
        soupBlockedUntilRelease = false;
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
}
