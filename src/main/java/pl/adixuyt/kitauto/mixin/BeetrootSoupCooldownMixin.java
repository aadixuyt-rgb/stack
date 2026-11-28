package pl.adixuyt.kitauto.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.adixuyt.kitauto.UseCooldownState;

/**
 * Applies separate use cooldowns before Minecraft performs the local item-use action.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class BeetrootSoupCooldownMixin {
    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void kitAuto$limitItemUse(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack heldStack = player.getStackInHand(hand);

        if (heldStack.isOf(Items.BEETROOT_SOUP) && UseCooldownState.shouldBlockSoupUse()) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        if (heldStack.isOf(Items.FIRE_CHARGE) && UseCooldownState.shouldBlockFireChargeUse()) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void kitAuto$limitFireChargeBlockUse(
            ClientPlayerEntity player,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack heldStack = player.getStackInHand(hand);
        if (heldStack.isOf(Items.FIRE_CHARGE) && UseCooldownState.shouldBlockFireChargeUse()) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
