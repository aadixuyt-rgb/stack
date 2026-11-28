package pl.adixuyt.kitauto.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.adixuyt.kitauto.RightClickUseLimiter;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class BeetrootSoupCooldownMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void kitAuto$filterRapidUsePackets(Packet<?> packet, CallbackInfo ci) {
        if (RightClickUseLimiter.shouldCancel(packet, MinecraftClient.getInstance())) {
            ci.cancel();
        }
    }
}
