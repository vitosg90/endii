package com.example.endcity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.LinkedHashMap;
import java.util.Map;

/** Звуки для оповещения. "none" = без звука. */
public final class Sounds {
    public static final Map<String, SoundEvent> SOUNDS = new LinkedHashMap<>();

    static {
        SOUNDS.put("none", null);
        SOUNDS.put("levelup", SoundEvents.PLAYER_LEVELUP);
        SOUNDS.put("beacon", SoundEvents.BEACON_ACTIVATE);
        SOUNDS.put("chime", SoundEvents.AMETHYST_BLOCK_CHIME);
        SOUNDS.put("enchant", SoundEvents.ENCHANTMENT_TABLE_USE);
        SOUNDS.put("xp", SoundEvents.EXPERIENCE_ORB_PICKUP);
        SOUNDS.put("ender_eye", SoundEvents.ENDER_EYE_LAUNCH);
        SOUNDS.put("allay", SoundEvents.ALLAY_ITEM_GIVEN);
        SOUNDS.put("totem", SoundEvents.TOTEM_USE);
    }

    private Sounds() {
    }

    public static void play(String key, float pitch) {
        SoundEvent ev = SOUNDS.get(key);
        int vol = DetectorConfig.get().volumePercent;
        if (ev == null || vol <= 0) return;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ev, pitch, vol / 100f));
    }
}
