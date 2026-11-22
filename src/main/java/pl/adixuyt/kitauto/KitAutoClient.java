package pl.adixuyt.kitauto;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KitAutoClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private static KeyBinding configKey;

    private static boolean enabled = false;
    private static boolean cycleActive = false;
    private static boolean gotAnyReward = false;
    private static boolean waitingForFinalGui = false;

    private static long tickCounter = 0;
    private static long nextKitTick = -1;
    private static long phaseStartedTick = 0;
    private static long nextClickTick = 0;
    private static int firstGuiSyncId = -1;

    private static boolean takePearls = true;
    private static boolean takeGapples = true;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kit_auto.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DELETE,
                "category.kit_auto"
        ));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kit_auto.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_SLASH,
                "category.kit_auto"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
    }

    private static void tick(MinecraftClient client) {
        tickCounter++;

        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            if (enabled) {
                nextKitTick = tickCounter;
                sendKit(client);
                nextKitTick = tickCounter + 2400;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§aKit Auto: WŁĄCZONY"), true);
                }
            } else {
                cycleActive = false;
                waitingForFinalGui = false;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§cKit Auto: WYŁĄCZONY"), true);
                }
            }
        }

        while (configKey.wasPressed()) {
            client.setScreen(new KitAutoConfigScreen());
        }

        if (!enabled || client.player == null || client.interactionManager == null
                || client.getNetworkHandler() == null) {
            return;
        }

        if (!cycleActive && tickCounter >= nextKitTick) {
            sendKit(client);
            nextKitTick += 2400;
        }

        if (!cycleActive || client.currentScreen == null) {
            return;
        }

        ScreenHandler handler = client.player.currentScreenHandler;
        if (handler == client.player.playerScreenHandler) {
            return;
        }

        // Etap 1: zwykłe kliknięcie szarego shulkera, aby go podnieść.
        if (!waitingForFinalGui) {
            // Najpierw szukamy szarego shulkera po typie przedmiotu.
            int targetSlot = -1;
            for (int i = 0; i < handler.slots.size(); i++) {
                ItemStack stack = handler.getSlot(i).getStack();
                if (!stack.isEmpty() && stack.isOf(Items.GRAY_SHULKER_BOX)) {
                    targetSlot = i;
                    break;
                }
            }

            // W menu „Zestawy serwerowe” szary zestaw jest w 3. rzędzie,
            // 3. kolumnie (slot 20 w 9-kolumnowym GUI).
            if (targetSlot == -1 && handler.slots.size() > 20
                    && !handler.getSlot(20).getStack().isEmpty()) {
                targetSlot = 20;
            }

            if (targetSlot != -1) {
                firstGuiSyncId = handler.syncId;
                client.interactionManager.clickSlot(
                        handler.syncId, targetSlot, 0, SlotActionType.PICKUP, client.player
                );
                waitingForFinalGui = true;
                phaseStartedTick = tickCounter;
                return;
            }
            return;
        }

        // Czekamy, aż serwer otworzy następne GUI (zwykle nowy syncId).
        if (handler.syncId == firstGuiSyncId) {
            if (tickCounter - phaseStartedTick > 80) {
                finishCycle(client);
            }
            return;
        }

        // Etap 2: szybkie przenoszenie nagród do ekwipunku przez Shift + klik.
        if (tickCounter < nextClickTick) return;
        for (int i = 0; i < handler.slots.size(); i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (stack.isEmpty()) continue;

            boolean wantedPearls = takePearls && stack.isOf(Items.ENDER_PEARL);
            boolean wantedGapples = takeGapples && stack.isOf(Items.ENCHANTED_GOLDEN_APPLE);

            if (wantedPearls || wantedGapples) {
                client.interactionManager.clickSlot(
                        handler.syncId, i, 0, SlotActionType.QUICK_MOVE, client.player
                );
                gotAnyReward = true;
                nextClickTick = tickCounter + 2;
                return;
            }
        }

        // Gdy nie ma już wybranych nagród, zamykamy końcowe GUI.
        if (gotAnyReward || tickCounter - phaseStartedTick > 120) {
            finishCycle(client);
        } else if (tickCounter - phaseStartedTick > 40) {
            finishCycle(client);
        }
    }

    private static void sendKit(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        client.getNetworkHandler().sendChatCommand("kit");
        cycleActive = true;
        waitingForFinalGui = false;
        gotAnyReward = false;
        firstGuiSyncId = -1;
        phaseStartedTick = tickCounter;
        nextClickTick = tickCounter;
    }

    private static void finishCycle(MinecraftClient client) {
        if (client.currentScreen != null && client.player != null
                && client.player.currentScreenHandler != client.player.playerScreenHandler) {
            client.player.closeHandledScreen();
        }
        cycleActive = false;
        waitingForFinalGui = false;
        gotAnyReward = false;
    }

    public static boolean takesPearls() { return takePearls; }
    public static boolean takesGapples() { return takeGapples; }
    public static void setTakePearls(boolean value) { takePearls = value; }
    public static void setTakeGapples(boolean value) { takeGapples = value; }
}
