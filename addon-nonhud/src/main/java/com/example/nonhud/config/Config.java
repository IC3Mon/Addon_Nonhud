package com.example.nonhud.config;

import com.example.nonhud.render.MoveMode;
import com.example.nonhud.render.Region;
import com.example.nonhud.render.RenderDefaults;
import com.example.nonhud.render.ScaleMode;
import com.example.nonhud.scan.Slot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * 模块 5 配置模型（设计文档 §6 config.json 草案）。
 *
 * 含渲染相关（fps/zOrder/regions/moveModes/effect）、
 * 槽位开关（slotsEnabled，O4）、按键（pause/settings）、
 * 数值阈值（thresholds，注入 NumericMonitor）。缺省值 = 设计 §6 默认 / RenderDefaults。
 */
public final class Config {

    public static final int VERSION = 1;

    /** 槽位开关（O4）：默认全开。 */
    public EnumSet<Slot> slotsEnabled = EnumSet.allOf(Slot.class);

    public double fps = RenderDefaults.FPS;

    /** 按键（字符串键名，如 "key.keyboard.p" / "key.keyboard.unknown"；空 = 默认）。 */
    public String pauseKey = "";
    public String settingsKey = "";

    /** Z 序（N2）。 */
    public List<Slot> zOrder = new ArrayList<>(RenderDefaults.zOrder());

    /** 每槽移动方式。 */
    public Map<Slot, MoveMode> moveModes = new EnumMap<>(Slot.class);
    /** 每槽显示区域。 */
    public Map<Slot, Region> regions = new EnumMap<>(Slot.class);

    /** 显示组（模块 5.1）：每组一个 area + 独立 fps/淡入淡出/重叠/层级；槽可多组多份显示。 */
    public List<com.example.nonhud.render.Group> groups =
            new ArrayList<>(com.example.nonhud.render.RenderDefaults.defaultGroups());

    public long fadeInMs = RenderDefaults.FADE_IN_MS;
    public long fadeOutMs = RenderDefaults.FADE_OUT_MS;
    public ScaleMode scaleMode = RenderDefaults.SCALE_MODE;

    /** 图片显示大小倍率（0.1–5.0，即 10%–500%；100% = 正常大小）。 */
    public double scalePercent = RenderDefaults.SCALE_PERCENT;

    /** numeric 槽一次性触发播放时长（毫秒，默认 3s；0 = 一直显示）。 */
    public long numericDurationMs = RenderDefaults.NUMERIC_DISPLAY_MS;

    /** 数值槽档位阈值（设计 §6）：energy / V槽 / A槽 / 妊娠。
     * V槽阈值 [30,60,100]：100% 满值 → 档位四（Vfullup 阶段四）。 */
    public List<Integer> energyThresholds = List.of(50, 100, 150);
    public List<Integer> vTankThresholds = List.of(30, 60, 100);
    public List<Integer> aTankThresholds = List.of(30, 60, 100);
    public List<Integer> pregnancyThresholds = List.of(85, 170, 255);

    /** fxxkme 周期触发（模块 5.1）：energy 阈值 + 随机间隔秒数区间。 */
    public int fxxkmeEnergyThreshold = 200;
    public int fxxkmeIntervalMinSec = 30;
    public int fxxkmeIntervalMaxSec = 60;

    /** 触发 botle 槽的 NoN 动画 ID（模块 5.1；实际动画 needsofnature:player_fill_bottle）。 */
    public String fillBottleAnimId = "player_fill_bottle";

    public Config() {
        // 缺省渲染参数与 RenderDefaults 对齐
        for (Slot s : Slot.values()) {
            moveModes.put(s, RenderDefaults.moveMode(s));
            regions.put(s, RenderDefaults.region(s));
        }
    }

    public Config copy() {
        Config c = new Config();
        c.slotsEnabled = EnumSet.copyOf(slotsEnabled);
        c.fps = fps;
        c.pauseKey = pauseKey;
        c.settingsKey = settingsKey;
        c.zOrder = new ArrayList<>(zOrder);
        c.moveModes = new EnumMap<>(moveModes);
        c.regions = new EnumMap<>(regions);
        c.groups = new ArrayList<>(groups);
        c.fadeInMs = fadeInMs;
        c.fadeOutMs = fadeOutMs;
        c.scaleMode = scaleMode;
        c.scalePercent = scalePercent;
        c.numericDurationMs = numericDurationMs;
        c.energyThresholds = new ArrayList<>(energyThresholds);
        c.vTankThresholds = new ArrayList<>(vTankThresholds);
        c.aTankThresholds = new ArrayList<>(aTankThresholds);
        c.pregnancyThresholds = new ArrayList<>(pregnancyThresholds);
        c.fxxkmeEnergyThreshold = fxxkmeEnergyThreshold;
        c.fxxkmeIntervalMinSec = fxxkmeIntervalMinSec;
        c.fxxkmeIntervalMaxSec = fxxkmeIntervalMaxSec;
        c.fillBottleAnimId = fillBottleAnimId;
        return c;
    }

    /** 槽位是否启用（O4）。 */
    public boolean slotEnabled(Slot slot) {
        return slotsEnabled.contains(slot);
    }
}
