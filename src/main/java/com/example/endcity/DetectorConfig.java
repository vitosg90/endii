package com.example.endcity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Настройки мода. Хранятся в config/endcitydetector.json. */
public class DetectorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DetectorConfig instance;

    public boolean enabled = true;
    /** true: работает только в измерении Энд. false: везде (удобно для проверки). */
    public boolean onlyInEnd = true;
    public boolean detectShulkers = true;
    public boolean detectPurpur = true;
    public boolean detectElytra = true;
    /** Сколько пурпурных блоков в одном чанке считать признаком города. */
    public int purpurThreshold = 12;
    /** Не повторять оповещение, если рядом (в блоках) уже было найдено. */
    public int minDistance = 128;
    public String sound = "levelup";
    public int volumePercent = 100;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("endcitydetector.json");
    }

    public static DetectorConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        DetectorConfig loaded = null;
        Path f = file();
        if (Files.exists(f)) {
            try (Reader r = Files.newBufferedReader(f)) {
                loaded = GSON.fromJson(r, DetectorConfig.class);
            } catch (Exception e) {
                System.err.println("[endcitydetector] Не удалось прочитать конфиг: " + e);
            }
        }
        instance = loaded != null ? loaded : new DetectorConfig();
        instance.fix();
    }

    public static void save() {
        if (instance == null) return;
        instance.fix();
        try (Writer w = Files.newBufferedWriter(file())) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            System.err.println("[endcitydetector] Не удалось сохранить конфиг: " + e);
        }
    }

    private void fix() {
        purpurThreshold = Math.max(3, Math.min(60, purpurThreshold));
        minDistance = Math.max(32, Math.min(512, minDistance));
        volumePercent = Math.max(0, Math.min(100, volumePercent));
        if (!Sounds.SOUNDS.containsKey(sound)) sound = "levelup";
    }
}
