package com.example.nonhud.scan;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * 一组同前缀帧（前缀_fYY 帧序列），或一张独立单图（不带 _fYY）。
 *
 * 帧推进语义（设计文档 §2.2/§2.3）：
 * - 帧序列：按 fps 循环推进帧索引；
 * - 单图：固定一帧，不循环、不自增帧。
 *
 * 帧索引是运行期播放状态；同前缀的帧列表为不可变共享数据（O1 重建后引用替换）。
 */
public final class FrameSequence {

    private final String prefix;
    private final List<Path> frames;
    private final boolean single;
    private final double fps;
    private int current;
    /** 未消费的分数帧步进（fps 小于 20 时单 tick 不足一帧，跨 tick 累积）。 */
    private double carry;

    public FrameSequence(String prefix, List<Path> frames, double fps, boolean single) {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("FrameSequence must have at least one frame");
        }
        this.prefix = Objects.requireNonNull(prefix);
        this.frames = List.copyOf(frames);
        this.fps = fps;
        this.single = single;
        this.current = 0;
    }

    /** 帧序列（带 _fYY，可循环）。 */
    public static FrameSequence sequence(String prefix, List<Path> frames, double fps) {
        return new FrameSequence(prefix, frames, fps, false);
    }

    /** 单张图（无 _fYY，固定一帧）。 */
    public static FrameSequence single(String prefix, Path frame) {
        return new FrameSequence(prefix, List.of(frame), 0.0, true);
    }

    public String prefix() {
        return prefix;
    }

    public List<Path> frames() {
        return frames;
    }

    public int size() {
        return frames.size();
    }

    public boolean single() {
        return single;
    }

    public double fps() {
        return fps;
    }

    public Path current() {
        return frames.get(current);
    }

    /**
     * 按 fps 推进 ticks 个游戏 tick（1/20 秒；支持小数，供渲染帧间隔折算）。
     * fps 语义为「每秒帧数」（设计文档 §2.2 默认 2fps）：帧数 = fps × 秒数 = fps × ticks/20。
     * 单图模式（single）或仅一帧时不推进；不足一帧的余数跨 tick 累积。
     */
    public void advanceTicks(double ticks) {
        if (single || frames.size() <= 1) {
            return;
        }
        carry += fps * ticks / 20.0;
        long steps = (long) Math.floor(carry);
        if (steps > 0) {
            current = (int) ((current + steps) % frames.size());
            carry -= steps;
        }
    }

    /**
     * 按帧数推进（模块 5.1 组播放速度：fps 按组独立设置，直接按「帧/秒 × 秒」推进）。
     * 单图模式（single）或仅一帧时不推进；不足一帧的余数跨帧累积。
     */
    public void advanceByFrames(double framesToAdvance) {
        if (single || frames.size() <= 1) {
            return;
        }
        carry += framesToAdvance;
        long steps = (long) Math.floor(carry);
        if (steps > 0) {
            current = (int) ((current + steps) % frames.size());
            carry -= steps;
        }
    }

    /** 回到首帧（进入阶段随机选中本序列后调用）。 */
    public void reset() {
        current = 0;
    }
}
