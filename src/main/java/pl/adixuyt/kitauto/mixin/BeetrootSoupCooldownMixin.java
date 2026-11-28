package pl.adixuyt.kitauto.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.adixuyt.kitauto.UseCooldownState;

/**
 * Routes soup and Fire Charge interactions through the shared cooldown tracker.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class BeetrootSoupCooldownMixin {
    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void kitAuto$checkItemUse(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack stack = player.getStackInHand(hand);
        if ((stack.isOf(Items.BEETROOT_SOUP) || stack.isOf(Items.FIRE_CHARGE))
                && UseCooldownState.shouldBlockUse(player, hand)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void kitAuto$finishItemUse(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        UseCooldownState.finishUse(hand, cir.getReturnValue());
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void kitAuto$checkBlockUse(
            ClientPlayerEntity player,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(Items.FIRE_CHARGE)
                && UseCooldownState.shouldBlockUse(player, hand)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void kitAuto$finishBlockUse(
            ClientPlayerEntity player,
            Hand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        UseCooldownState.finishUse(hand, cir.getReturnValue());
    }
}
