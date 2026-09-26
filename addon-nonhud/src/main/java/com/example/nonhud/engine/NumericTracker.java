package com.example.nonhud.engine;

import com.example.nonhud.scan.Direction;

import java.util.List;
import java.util.Optional;

/**
 * numeric 槽滞回状态机：持续采样数值，仅在「穿越阈值（档位变化）」时报告变更。
 *
 * - 首次采样即报告（进入世界时玩家已有档位 → 直接显示对应档）。
 * - 档位不变（数值小幅波动未穿档）不报告。
 * - 档位变化时同时携带方向（up/re，用于选子目录）。
 */
public final class NumericTracker {

    private long lastValue;
    private int currentPh;
    private Direction lastDirection;
    private boolean initialized;

    /** 一次档位变更：新档位 + 触发方向（首次采样方向为 null，由调用方决定默认值）。 */
    public record Change(int ph, Direction direction) { }

    public NumericTracker() {
        this.currentPh = 1;
    }

    public Optional<Change> update(long value, List<Integer> thresholds) {
        int ph = NumericTier.tierForValue(value, thresholds);
        Direction dir = initialized ? NumericTier.directionFor(lastValue, value) : null;
        boolean changed = !initialized || ph != currentPh;

        lastValue = value;
        currentPh = ph;
        if (dir != null) {
            lastDirection = dir;
        }
        initialized = true;

        if (!changed) {
            return Optional.empty();
        }
        return Optional.of(new Change(ph, dir));
    }

    public int currentPh() {
        return currentPh;
    }

    public Direction lastDirection() {
        return lastDirection;
    }

    /** 重置状态（玩家性别变更换槽 / 妊娠重新开始时）。 */
    public void reset() {
        lastValue = 0;
        currentPh = 1;
        lastDirection = null;
        initialized = false;
    }
}
