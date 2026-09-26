package com.example.nonhud.render;

import com.example.nonhud.scan.Slot;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 模块 4 渲染默认配置（设计文档 §6 config.json 草案的默认值；模块 5 配置化后由 Config 覆盖）。
 */
public final class RenderDefaults {

    /** 全局 fps（每秒帧数；§6 fps: 2）。 */
    public static final double FPS = 2.0;

    /** 缓存刷新轮询间隔（毫秒；§6 cache.refreshInterval: 2000）。 */
    public static final long CACHE_REFRESH_MS = 2000;

    /** 淡入时长（毫秒；§6 effect.fadeInMs: 300）。 */
    public static final long FADE_IN_MS = 300;

    /** 淡出时长（毫秒；§6 effect.fadeOutMs: 300）。 */
    public static final long FADE_OUT_MS = 300;

    /** 缩放方式（§6 effect.scaleMode: "fit"）。 */
    public static final ScaleMode SCALE_MODE = ScaleMode.FIT;

    /** 图片显示大小倍率（10%–500%；100% = 区域 FIT/拉伸后的正常大小）。 */
    public static final double SCALE_PERCENT = 1.0;

    /** numeric 槽一次性触发播放时长（毫秒；默认 3s，可配置）。 */
    public static final long NUMERIC_DISPLAY_MS = 3000;

    private static volatile ScaleMode scaleMode = SCALE_MODE;

    public static ScaleMode scaleMode() {
        return scaleMode;
    }

    public static void setScaleMode(ScaleMode mode) {
        scaleMode = mode == null ? ScaleMode.FIT : mode;
    }

    private static volatile double scalePercent = SCALE_PERCENT;

    /** 当前图片显示大小倍率（0.1–5.0）。 */
    public static double scalePercent() {
        return scalePercent;
    }

    /** 设置图片显示大小倍率（越界收敛到 10%–500%）。 */
    public static void setScalePercent(double value) {
        scalePercent = Math.max(0.1, Math.min(5.0, value));
    }

    private static volatile long numericDisplayMs = NUMERIC_DISPLAY_MS;

    /** 当前 numeric 槽一次性触发播放时长（毫秒；0 = 一直显示）。 */
    public static long numericDisplayMs() {
        return numericDisplayMs;
    }

    public static void setNumericDisplayMs(long ms) {
        numericDisplayMs = Math.max(0, ms);
    }

    /** 随机移动重新选点间隔（毫秒；待定点 5 低频）。 */
    public static final long RANDOM_REFRESH_MS = 500;

    /** 随机移动半径（像素；待定点 5 小范围）。 */
    public static final int RANDOM_DISTANCE = 4;

    private static final Map<Slot, MoveMode> MOVE_MODES = new EnumMap<>(Slot.class);
    private static final Map<Slot, Region> REGIONS = new EnumMap<>(Slot.class);
    private static volatile List<Slot> zOrder = ZOrder.defaults();
    private static volatile double fps = FPS;
    private static volatile long fadeInMs = FADE_IN_MS;
    private static volatile long fadeOutMs = FADE_OUT_MS;

    public static long fadeInMs() {
        return fadeInMs;
    }

    public static void setFadeInMs(long ms) {
        fadeInMs = Math.max(0, ms);
    }

    public static long fadeOutMs() {
        return fadeOutMs;
    }

    public static void setFadeOutMs(long ms) {
        fadeOutMs = Math.max(0, ms);
    }

    static {
        // §6 moveMode 草案默认：V 方向偏移(0,-8)，M 随机，其余固定
        MOVE_MODES.put(Slot.A, MoveMode.fixed());
        MOVE_MODES.put(Slot.V, MoveMode.direction(0, -8));
        MOVE_MODES.put(Slot.M, MoveMode.random(RANDOM_DISTANCE, RANDOM_REFRESH_MS));
        MOVE_MODES.put(Slot.BOTLE, MoveMode.fixed());
        MOVE_MODES.put(Slot.VFULL, MoveMode.fixed());
        MOVE_MODES.put(Slot.AFULL, MoveMode.fixed());
        MOVE_MODES.put(Slot.PREGNANT, MoveMode.fixed());
        MOVE_MODES.put(Slot.FXXKME, MoveMode.fixed());

        // 默认区域：各槽在屏幕不同区块（模块 5 设置界面可改）。
        // 竖版人物图集按 FIT 以高度为约束，区域高度决定角色大小。
        // 参考 2560x1440 + guiScale4 → 缩放屏 640x360：两行 150x160，角色约 1/2 屏高。
        REGIONS.put(Slot.A, new Region(20, 30, 150, 160));
        REGIONS.put(Slot.V, new Region(250, 30, 150, 160));
        REGIONS.put(Slot.M, new Region(480, 30, 150, 160));
        REGIONS.put(Slot.BOTLE, new Region(250, 5, 150, 160));
        REGIONS.put(Slot.VFULL, new Region(20, 195, 150, 160));
        REGIONS.put(Slot.AFULL, new Region(250, 195, 150, 160));
        REGIONS.put(Slot.PREGNANT, new Region(480, 195, 150, 160));
        REGIONS.put(Slot.FXXKME, new Region(250, 100, 150, 160));
    }

    private RenderDefaults() { }

    public static double fps() {
        return fps;
    }

    public static void setFps(double value) {
        fps = Math.max(0.1, value);
    }

    public static MoveMode moveMode(Slot slot) {
        return MOVE_MODES.getOrDefault(slot, MoveMode.fixed());
    }

    public static void setMoveMode(Slot slot, MoveMode mode) {
        MOVE_MODES.put(slot, mode);
    }

    public static Region region(Slot slot) {
        return REGIONS.getOrDefault(slot, new Region(0, 0, 90, 90));
    }

    public static void setRegion(Slot slot, Region region) {
        REGIONS.put(slot, region);
    }

    /** 当前 Z 序（N2）。 */
    public static List<Slot> zOrder() {
        return zOrder;
    }

    public static void setZOrder(List<Slot> value) {
        zOrder = List.copyOf(value);
    }

    /** 每槽移动方式快照（不可变）。 */
    public static Map<Slot, MoveMode> moveModesSnapshot() {
        return Map.copyOf(MOVE_MODES);
    }
}
