package com.example.endcity;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** Настройки детектора. */
public class SettingsScreen extends Screen {
    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Component.translatable("endcity.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        DetectorConfig c = DetectorConfig.get();
        int cw = 150;
        int lx = this.width / 2 - 155;
        int rx = this.width / 2 + 5;
        int step = 24;
        int y0 = Math.max(26, (this.height - 5 * step - 30) / 2);

        // Левая колонка: что искать.
        int y = y0;
        addRenderableWidget(toggle(lx, y, cw, "endcity.opt.enabled", () -> c.enabled, v -> c.enabled = v));
        y += step;
        addRenderableWidget(toggle(lx, y, cw, "endcity.opt.only_end", () -> c.onlyInEnd, v -> c.onlyInEnd = v));
        y += step;
        addRenderableWidget(toggle(lx, y, cw, "endcity.opt.shulkers", () -> c.detectShulkers, v -> c.detectShulkers = v));
        y += step;
        addRenderableWidget(toggle(lx, y, cw, "endcity.opt.purpur", () -> c.detectPurpur, v -> c.detectPurpur = v));
        y += step;
        addRenderableWidget(toggle(lx, y, cw, "endcity.opt.elytra", () -> c.detectElytra, v -> c.detectElytra = v));

        // Правая колонка: оповещение и чувствительность.
        y = y0;
        addRenderableWidget(soundButton(rx, y, cw, c));
        y += step;
        addRenderableWidget(new IntSlider(rx, y, cw, 0, 100, c.volumePercent,
                v -> Component.translatable("endcity.opt.volume", v), v -> c.volumePercent = v));
        y += step;
        addRenderableWidget(new IntSlider(rx, y, cw, 3, 60, c.purpurThreshold,
                v -> Component.translatable("endcity.opt.threshold", v), v -> c.purpurThreshold = v));
        y += step;
        addRenderableWidget(new IntSlider(rx, y, cw, 32, 512, c.minDistance,
                v -> Component.translatable("endcity.opt.distance", v), v -> c.minDistance = v));

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
                .bounds(this.width / 2 - 100, y0 + 5 * step + 8, 200, 20).build());
    }

    private Button toggle(int x, int y, int w, String key, BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(toggleLabel(key, get.getAsBoolean()), b -> {
            boolean now = !get.getAsBoolean();
            set.accept(now);
            b.setMessage(toggleLabel(key, now));
        }).bounds(x, y, w, 20).build();
    }

    private static Component toggleLabel(String key, boolean on) {
        return Component.translatable(key, Component.translatable(on ? "options.on" : "options.off"));
    }

    private Button soundButton(int x, int y, int w, DetectorConfig c) {
        List<String> keys = new ArrayList<>(Sounds.SOUNDS.keySet());
        return Button.builder(soundLabel(c.sound), b -> {
            int idx = keys.indexOf(c.sound);
            c.sound = keys.get((idx + 1) % keys.size());
            b.setMessage(soundLabel(c.sound));
            Sounds.play(c.sound, 1.0f);
        }).bounds(x, y, w, 20).build();
    }

    private static Component soundLabel(String key) {
        return Component.translatable("endcity.opt.sound", Component.translatable("endcity.sound." + key));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        String t = this.title.getString();
        graphics.text(this.font, t, (this.width - this.font.width(t)) / 2, 10, 0xFFFFFFFF, true);
    }

    @Override
    public void onClose() {
        DetectorConfig.save();
        this.minecraft.gui.setScreen(parent);
    }

    private static class IntSlider extends AbstractSliderButton {
        private final int min;
        private final int max;
        private final IntFunction<Component> label;
        private final IntConsumer setter;

        IntSlider(int x, int y, int w, int min, int max, int current,
                  IntFunction<Component> label, IntConsumer setter) {
            super(x, y, w, 20, Component.empty(), (current - min) / (double) (max - min));
            this.min = min;
            this.max = max;
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        private int current() {
            return min + (int) Math.round(this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            if (label == null) return; // вызов из конструктора родителя
            setMessage(label.apply(current()));
        }

        @Override
        protected void applyValue() {
            setter.accept(current());
        }
    }
}
