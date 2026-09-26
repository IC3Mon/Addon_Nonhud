package com.example.nonhud.render;

import com.example.nonhud.scan.FrameSequence;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;

import java.util.Objects;
import java.util.Random;

/**
 * 单个槽位的渲染实例状态（设计文档 §5：持有 slot/stage/当前帧纹理/显示区域/移动方式/透明度）。
 *
 * 纯逻辑状态机，便于单元测试：
 * - 激活：alpha 0 → 1 淡入（fadeInMs），每帧推进帧序列；
 * - 停用：alpha 1 → 0 淡出（fadeOutMs），结束后由渲染器移除；
 * - 帧推进：按 fps（每秒帧数）推进帧序号（跨帧累积 carry，同 FrameSequence 语义）。
 *
 * 不持有 GL 资源：纹理由 TextureCache 按 currentFramePath() 缓存装载。
 */
public final class SlotRenderState {

    /** 屏幕绘制结果：目标矩形 + 透明度 + 当前帧路径。 */
    public record Draw(int x, int y, int width, int height, float alpha, java.nio.file.Path framePath, boolean visible) { }

    private final Slot slot;
    private final SlotKey key;
    private final FrameSequence seq;
    private final Region region;
    private final MoveMode moveMode;
    private final ScaleMode scaleMode;
    /** 图片显示大小倍率（0.1–5.0，100% = 区域 FIT/拉伸后的正常大小）。 */
    private final double scalePercent;
    private final long fadeInMs;
    private final long fadeOutMs;
    private final Random random;

    private boolean active;
    /** 已播放总毫秒（驱动帧推进与 RANDOM 移动选点）。 */
    private long timeMs;
    /** 当前淡入已流逝毫秒。 */
    private long fadeInElapsed;
    /** 当前淡出已流逝毫秒。 */
    private long fadeOutElapsed;
    private float alpha;
    /** RANDOM 模式：自上次重选以来的毫秒数。 */
    private long moveAccumMs;

    public SlotRenderState(Slot slot, SlotKey key, FrameSequence seq, Region region,
                           MoveMode moveMode, ScaleMode scaleMode, double scalePercent,
                           long fadeInMs, long fadeOutMs, Random random) {
        this.slot = Objects.requireNonNull(slot);
        this.key = Objects.requireNonNull(key);
        this.seq = Objects.requireNonNull(seq);
        this.region = Objects.requireNonNull(region);
        this.moveMode = Objects.requireNonNull(moveMode);
        this.scaleMode = Objects.requireNonNull(scaleMode);
        this.scalePercent = Math.max(0.1, Math.min(5.0, scalePercent));
        this.fadeInMs = Math.max(0, fadeInMs);
        this.fadeOutMs = Math.max(0, fadeOutMs);
        this.random = Objects.requireNonNull(random);
        this.active = true;
        this.alpha = 0f; // 淡入起点
    }

    public Slot slot() {
        return slot;
    }

    public SlotKey key() {
        return key;
    }

    public boolean active() {
        return active;
    }

    public float alpha() {
        return alpha;
    }

    public java.nio.file.Path currentFramePath() {
        return seq.current();
    }

    /** 序列全部帧路径（不可变；供纹理缓存释放用，O2）。 */
    public java.util.List<java.nio.file.Path> sequenceFrames() {
        return seq.frames();
    }

    /** 切到停用：进入淡出（重置淡出计时）。重复调用无副作用。 */
    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        fadeOutElapsed = 0;
    }

    /**
     * 推进 deltaMs 毫秒：
     * - 推进帧序列（fps 语义=每秒帧数）；
     * - 更新淡入/淡出 alpha；
     * - 累积移动偏移周期。
     */
    public void advance(long deltaMs) {
        if (deltaMs <= 0) {
            return;
        }
        timeMs += deltaMs;
        // 渲染帧间隔折算成游戏 tick（含小数）；FrameSequence 内部 carry 跨帧累积分数步
        seq.advanceTicks(deltaMs * 20.0 / 1000.0);
        if (active) {
            fadeInElapsed += deltaMs;
            alpha = fadeInMs <= 0 ? 1f : Math.min(1f, (float) fadeInElapsed / fadeInMs);
        } else {
            fadeOutElapsed += deltaMs;
            alpha = fadeOutMs <= 0 ? 0f : Math.max(0f, 1f - (float) fadeOutElapsed / fadeOutMs);
        }
        moveAccumMs += deltaMs;
    }

    /**
     * 本帧绘制矩形：按 scaleMode 在区域内等比例/拉伸，再叠加上移动偏移。
     *
     * @param imgW 图片原始宽度（像素）
     * @param imgH 图片原始高度（像素）
     */
    public Draw draw(int imgW, int imgH) {
        int w;
        int h;
        if (imgW <= 0 || imgH <= 0) {
            return new Draw(region.x(), region.y(), 0, 0, alpha, seq.current(), alpha > 0f);
        }
        if (scaleMode == ScaleMode.STRETCH) {
            w = region.width();
            h = region.height();
        } else {
            double scale = Math.min((double) region.width() / imgW, (double) region.height() / imgH);
            w = (int) Math.max(1, Math.round(imgW * scale));
            h = (int) Math.max(1, Math.round(imgH * scale));
        }
        // 图片显示大小倍率：100% = 区域 FIT/拉伸后的正常大小，可放大到 500%（越界居中对齐区域）
        w = (int) Math.max(1, Math.round(w * scalePercent));
        h = (int) Math.max(1, Math.round(h * scalePercent));
        MoveMode.Offset off = moveMode.offset(random, timeMs, moveAccumMs);
        int x = region.x() + (region.width() - w) / 2 + off.x();
        int y = region.y() + (region.height() - h) / 2 + off.y();
        return new Draw(x, y, w, h, alpha, seq.current(), alpha > 0f);
    }
}
