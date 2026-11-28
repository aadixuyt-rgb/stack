package pl.adixuyt.kitauto.mixin;

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
import pl.adixuyt.kitauto.UseCooldownState;

/**
 * Applies a one-second cooldown after each successful beetroot-soup use.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class BeetrootSoupCooldownMixin {
    @Unique
    private boolean kitAuto$soupUseAttemptPending;

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void kitAuto$checkSoupCooldown(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        kitAuto$soupUseAttemptPending = false;

        ItemStack heldStack = player.getStackInHand(hand);
        if (!heldStack.isOf(Items.BEETROOT_SOUP)) {
            return;
        }

        if (UseCooldownState.isSoupCoolingDown()) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        kitAuto$soupUseAttemptPending = true;
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void kitAuto$recordSuccessfulSoupUse(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        if (!kitAuto$soupUseAttemptPending) {
            return;
        }

        kitAuto$soupUseAttemptPending = false;
        ActionResult result = cir.getReturnValue();
        if (result != null && result.isAccepted()) {
            UseCooldownState.recordSuccessfulSoupUse();
        }
    }
}
