package pl.adixuyt.kitauto.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class BeetrootSoupCooldownMixin {
    @Unique
    private static final long KIT_AUTO_SOUP_COOLDOWN_NANOS = 1_500_000_000L;

    @Unique
    private static long kitAuto$lastBeetrootSoupPacketNanos = 0L;

    /**
     * Cancels repeated beetroot-soup use packets before they are sent to the server.
     */
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void kitAuto$filterRapidSoupUsePackets(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof PlayerInteractItemC2SPacket itemPacket)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        ItemStack heldStack = client.player.getStackInHand(itemPacket.getHand());
        if (!heldStack.isOf(Items.BEETROOT_SOUP)) {
            return;
        }

        long now = System.nanoTime();
        if (kitAuto$lastBeetrootSoupPacketNanos != 0L
                && now - kitAuto$lastBeetrootSoupPacketNanos < KIT_AUTO_SOUP_COOLDOWN_NANOS) {
            ci.cancel();
            return;
        }

        kitAuto$lastBeetrootSoupPacketNanos = now;
    }
}
