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

@Mixin(ClientPlayerInteractionManager.class)
public abstract class BeetrootSoupCooldownMixin {
    @Unique
    private static long kitAuto$lastBeetrootSoupUseNanos = 0L;

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void kitAuto$limitBeetrootSoupUse(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(Items.BEETROOT_SOUP)) {
            return;
        }

        long now = System.nanoTime();
        if (now - kitAuto$lastBeetrootSoupUseNanos < 1_500_000_000L) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        kitAuto$lastBeetrootSoupUseNanos = now;
    }
}
