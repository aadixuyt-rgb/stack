package pl.adixuyt.kitauto;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Locale;

public class KitAutoConfigScreen extends Screen {
    public KitAutoConfigScreen() {
        super(Text.literal("Kit Auto — konfiguracja"));
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int y = this.height / 2 - 75;

        addDrawableChild(ButtonWidget.builder(pearlsLabel(), button -> {
            KitAutoClient.setTakePearls(!KitAutoClient.takesPearls());
            button.setMessage(pearlsLabel());
        }).dimensions(center - 100, y, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(gappleLabel(), button -> {
            KitAutoClient.setTakeGapples(!KitAutoClient.takesGapples());
            button.setMessage(gappleLabel());
        }).dimensions(center - 100, y + 26, 200, 20).build());

        addDrawableChild(new SliderWidget(center - 110, y + 62, 220, 20,
                intervalLabel(), (KitAutoClient.getKitIntervalTicks() - 600) / 20.0) {
            @Override
            protected void updateMessage() {
                setMessage(intervalLabel());
            }

            @Override
            protected void applyValue() {
                int step = (int) Math.round(this.value * 10.0);
                KitAutoClient.setKitIntervalTicks(600 + step * 2);
                updateMessage();
            }
        });

        addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), button -> close())
                .dimensions(center - 100, y + 96, 200, 20).build());
    }

    private static Text pearlsLabel() {
        return Text.literal("Ender Pearls: " + (KitAutoClient.takesPearls() ? "ON" : "OFF"));
    }

    private static Text gappleLabel() {
        return Text.literal("Enchanted Golden Apples: " + (KitAutoClient.takesGapples() ? "ON" : "OFF"));
    }

    private static Text intervalLabel() {
        double seconds = KitAutoClient.getKitIntervalTicks() / 20.0;
        String formatted = String.format(Locale.ROOT, "%.1f", seconds).replace('.', ',');
        return Text.literal("Odstęp: " + formatted + " s  |  " + KitAutoClient.getKitIntervalTicks() + " ticków");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 105, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Wybierz przedmioty odbierane z ostatniego GUI"),
                width / 2, height / 2 - 90, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
