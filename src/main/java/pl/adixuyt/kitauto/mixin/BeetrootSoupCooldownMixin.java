package pl.adixuyt.kitauto.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.adixuyt.kitauto.UseCooldownState;

/**
 * Limits beetroot soup use before Minecraft performs the local item-use action.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class BeetrootSoupCooldownMixin {
    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void kitAuto$limitSoupUse(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack heldStack = player.getStackInHand(hand);
        if (heldStack.isOf(Items.BEETROOT_SOUP) && UseCooldownState.shouldBlockSoupUse()) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
