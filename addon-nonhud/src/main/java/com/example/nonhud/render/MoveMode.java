package com.example.nonhud.render;

import java.util.Objects;
import java.util.Random;

/**
 * 槽位移动方式（设计文档 §4.4「移动方式」：固定 / 方向 / 随机）。
 * - FIXED：固定在区域位置，无偏移；
 * - DIRECTION：沿 (dx, dy) 方向持续偏移（低频率小范围，像素级）；
 * - RANDOM：以区域中心为圆心、半径为 distance 的范围内随机偏移，每 refreshMs 重选一次（待定点 5：低频小范围）。
 */
public final class MoveMode {

    public enum Type { FIXED, DIRECTION, RANDOM }

    private final Type type;
    private final int dx;
    private final int dy;
    /** RANDOM 模式：偏移半径（像素）。 */
    private final int distance;
    /** RANDOM 模式：重新随机偏移的间隔（毫秒）。 */
    private final long refreshMs;

    public MoveMode(Type type, int dx, int dy, int distance, long refreshMs) {
        this.type = Objects.requireNonNull(type);
        this.dx = dx;
        this.dy = dy;
        this.distance = distance;
        this.refreshMs = refreshMs;
    }

    public static MoveMode fixed() {
        return new MoveMode(Type.FIXED, 0, 0, 0, 0);
    }

    public static MoveMode direction(int dx, int dy) {
        return new MoveMode(Type.DIRECTION, dx, dy, 0, 0);
    }

    public static MoveMode random(int distance, long refreshMs) {
        return new MoveMode(Type.RANDOM, 0, 0, distance, refreshMs);
    }

    public Type type() {
        return type;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public int distance() {
        return distance;
    }

    public long refreshMs() {
        return refreshMs;
    }

    /**
     * 计算本帧偏移 (x, y)；FIXED 返回 (0,0)。
     * @param random  随机源（可注入以便测试）
     * @param timeMs  该槽已播放的总毫秒数（RANDOM 按刷新周期推导当前偏移）
     */
    public Offset offset(Random random, long timeMs, long accumulator) {
        return switch (type) {
            case FIXED -> new Offset(0, 0);
            case DIRECTION -> new Offset(dx, dy);
            case RANDOM -> {
                int half = Math.max(0, distance);
                // 以区域中心为圆心，[-half, half] 随机偏移；周期内用 (timeMs/refreshMs) 推导 seed，保证同周期稳定
                long period = Math.max(1, refreshMs);
                long cycle = timeMs / period;
                long seed = cycle * 0x9E3779B97F4A7C15L ^ random.nextLong();
                long x = (seed & 0xFFFF) % (2L * half + 1) - half;
                long y = ((seed >>> 16) & 0xFFFF) % (2L * half + 1) - half;
                yield new Offset((int) x, (int) y);
            }
        };
    }

    public record Offset(int x, int y) { }
}
