package pl.adixuyt.kitauto.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies separate use cooldowns to beetroot soup and the Fire Charge item.
 * Both are checked before Minecraft performs the local item-use action.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class BeetrootSoupCooldownMixin {
    @Unique
    private static final long KIT_AUTO_SOUP_COOLDOWN_NANOS = 1_500_000_000L;

    @Unique
    private static final long KIT_AUTO_FIRE_CHARGE_COOLDOWN_NANOS = 1_000_000_000L;

    @Unique
    private static long kitAuto$lastSoupUseNanos = 0L;

    @Unique
    private static long kitAuto$lastFireChargeUseNanos = 0L;

    @Unique
    private static boolean kitAuto$soupBlockedUntilRelease = false;

    @Unique
    private static boolean kitAuto$fireChargeBlockedUntilRelease = false;

    /**
     * Reset the per-hold lock only after the physical use button is released.
     * The timestamp-based cooldown remains active after release.
     */
    public static void updateReleaseState(MinecraftClient client) {
        if (client == null || client.options == null || client.options.useKey.isPressed()) {
            return;
        }

        kitAuto$soupBlockedUntilRelease = false;
        kitAuto$fireChargeBlockedUntilRelease = false;
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void kitAuto$limitItemUse(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack heldStack = player.getStackInHand(hand);
        long now = System.nanoTime();

        if (heldStack.isOf(Items.BEETROOT_SOUP)
                && kitAuto$shouldBlockSoup(now)) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        if (heldStack.isOf(Items.FIRE_CHARGE)
                && kitAuto$shouldBlockFireCharge(now)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void kitAuto$limitFireChargeBlockUse(
            PlayerEntity player,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack heldStack = player.getStackInHand(hand);
        if (!heldStack.isOf(Items.FIRE_CHARGE)) {
            return;
        }

        if (kitAuto$shouldBlockFireCharge(System.nanoTime())) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Unique
    private static boolean kitAuto$shouldBlockSoup(long now) {
        if (kitAuto$soupBlockedUntilRelease
                || (kitAuto$lastSoupUseNanos != 0L
                && now - kitAuto$lastSoupUseNanos < KIT_AUTO_SOUP_COOLDOWN_NANOS)) {
            kitAuto$soupBlockedUntilRelease = true;
            return true;
        }

        kitAuto$lastSoupUseNanos = now;
        kitAuto$soupBlockedUntilRelease = true;
        return false;
    }

    @Unique
    private static boolean kitAuto$shouldBlockFireCharge(long now) {
        if (kitAuto$fireChargeBlockedUntilRelease
                || (kitAuto$lastFireChargeUseNanos != 0L
                && now - kitAuto$lastFireChargeUseNanos < KIT_AUTO_FIRE_CHARGE_COOLDOWN_NANOS)) {
            kitAuto$fireChargeBlockedUntilRelease = true;
            return true;
        }

        kitAuto$lastFireChargeUseNanos = now;
        kitAuto$fireChargeBlockedUntilRelease = true;
        return false;
    }
}
