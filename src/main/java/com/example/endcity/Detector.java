package com.example.endcity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Ищет признаки Энд-городов среди того, что сервер прислал клиенту:
 * шалкеры, пурпурные блоки, рамки с элитрами (корабль Энда).
 */
public final class Detector {
    // Высоты, на которых бывают города на внешних островах Энда.
    private static final int SCAN_MIN_Y = 40;
    private static final int SCAN_MAX_Y = 130;
    private static final int FRAME_CHECK_DELAY = 30; // тиков: данные рамки приходят чуть позже сущности

    private static final List<BlockPos> found = new ArrayList<>();
    private static final List<PendingFrame> pending = new ArrayList<>();

    private static final class PendingFrame {
        final ItemFrame frame;
        int ticksLeft;

        PendingFrame(ItemFrame frame, int ticksLeft) {
            this.frame = frame;
            this.ticksLeft = ticksLeft;
        }
    }

    private Detector() {
    }

    public static void reset() {
        found.clear();
        pending.clear();
    }

    private static boolean inScope(Level level) {
        DetectorConfig c = DetectorConfig.get();
        return c.enabled && (!c.onlyInEnd || level.dimension() == Level.END);
    }

    public static void onEntityLoad(Entity entity, Level level) {
        DetectorConfig c = DetectorConfig.get();
        if (!inScope(level)) return;
        if (c.detectShulkers && isShulker(entity)) {
            alert("shulker", entity.blockPosition());
        } else if (c.detectElytra && entity instanceof ItemFrame frame) {
            pending.add(new PendingFrame(frame, FRAME_CHECK_DELAY));
        }
    }

    public static void onChunkLoad(Level level, LevelChunk chunk) {
        DetectorConfig c = DetectorConfig.get();
        if (!c.detectPurpur || !inScope(level)) return;

        ChunkPos cp = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int count = 0;
        BlockPos first = null;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = SCAN_MIN_Y; y < SCAN_MAX_Y; y++) {
                    pos.set(cp.getMinBlockX() + x, y, cp.getMinBlockZ() + z);
                    BlockState state = chunk.getBlockState(pos);
                    if (isPurpur(state)) {
                        count++;
                        if (first == null) first = pos.immutable();
                    }
                }
            }
        }
        if (first != null && count >= c.purpurThreshold) {
            alert("purpur", first);
        }
    }

    private static boolean isShulker(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals("shulker");
    }

    private static boolean isPurpur(BlockState s) {
        return s.is(Blocks.PURPUR_BLOCK) || s.is(Blocks.PURPUR_PILLAR) || s.is(Blocks.PURPUR_STAIRS);
    }

    /** Вызывается каждый тик: проверяем рамки, которым уже пришли данные. */
    public static void tick() {
        if (pending.isEmpty()) return;
        DetectorConfig c = DetectorConfig.get();
        Iterator<PendingFrame> it = pending.iterator();
        while (it.hasNext()) {
            PendingFrame p = it.next();
            if (p.frame.isRemoved()) {
                it.remove();
                continue;
            }
            if (--p.ticksLeft > 0) continue;
            it.remove();
            if (c.detectElytra && p.frame.getItem().is(Items.ELYTRA)) {
                alert("elytra", p.frame.blockPosition());
            }
        }
    }

    private static void alert(String kind, BlockPos pos) {
        DetectorConfig c = DetectorConfig.get();
        long minSq = (long) c.minDistance * c.minDistance;
        for (BlockPos p : found) {
            if (p.distSqr(pos) < minSq) return; // рядом уже оповещали
        }
        found.add(pos);

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        int dist = (int) Math.sqrt(player.blockPosition().distSqr(pos));
        Component msg = Component.translatable("endcity.alert." + kind,
                pos.getX(), pos.getY(), pos.getZ(), dist).withStyle(ChatFormatting.LIGHT_PURPLE);
        player.sendSystemMessage(msg);
        Sounds.play(c.sound, 1.0f);
    }

    /** Печатает в чат список найденных признаков. */
    public static void printFound() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (found.isEmpty()) {
            player.sendSystemMessage(Component.translatable("endcity.list.empty"));
            return;
        }
        player.sendSystemMessage(Component.translatable("endcity.list.header", found.size())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        for (int i = 0; i < found.size(); i++) {
            BlockPos p = found.get(i);
            int dist = (int) Math.sqrt(player.blockPosition().distSqr(p));
            player.sendSystemMessage(Component.translatable("endcity.list.entry",
                    i + 1, p.getX(), p.getY(), p.getZ(), dist));
        }
    }
}
