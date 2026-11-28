package pl.adixuyt.kitauto.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Limits beetroot soup at the client item-use entry point, before Minecraft
 * performs the use action or sends its interaction packet.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class BeetrootSoupCooldownMixin {
    @Unique
    private static final long KIT_AUTO_SOUP_COOLDOWN_NANOS = 1_500_000_000L;

    @Unique
    private static long kitAuto$lastSoupUseNanos = 0L;

    @Unique
    private static boolean kitAuto$soupBlockedUntilRelease = false;

    /**
     * Called on client ticks. A held right mouse button stays blocked after one
     * accepted soup use until the player releases it.
     */
    public static void updateReleaseState(MinecraftClient client) {
        if (client == null || client.options == null || client.options.useKey.isPressed()) {
            return;
        }
        kitAuto$soupBlockedUntilRelease = false;
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void kitAuto$limitBeetrootSoupUse(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack heldStack = player.getStackInHand(hand);
        if (!heldStack.isOf(Items.BEETROOT_SOUP)) {
            return;
        }

        long now = System.nanoTime();
        if (kitAuto$soupBlockedUntilRelease
                || (kitAuto$lastSoupUseNanos != 0L
                && now - kitAuto$lastSoupUseNanos < KIT_AUTO_SOUP_COOLDOWN_NANOS)) {
            kitAuto$soupBlockedUntilRelease = true;
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        kitAuto$lastSoupUseNanos = now;
        kitAuto$soupBlockedUntilRelease = true;
    }
}
