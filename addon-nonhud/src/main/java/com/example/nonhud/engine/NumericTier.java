package com.example.nonhud.engine;

import com.example.nonhud.scan.Direction;
import com.example.nonhud.scan.Gender;
import com.example.nonhud.scan.Slot;

import java.util.List;

/**
 * numeric 槽数值分档（设计文档 §3.3）：值 → phX 档位；前后值比较 → up/re 方向。
 *
 * 档位语义：ph = 1 + 命中的阈值个数（value ≥ 阈值即命中一档）。
 * 例：energy 阈值 [50,100,150] → 0–49=ph1，50–99=ph2，100–149=ph3，150–200=ph4。
 */
public final class NumericTier {

    private NumericTier() { }

    /** 值 → 档位（1 基）。阈值为空或值小于所有阈值 → 1。 */
    public static int tierForValue(long value, List<Integer> thresholds) {
        int ph = 1;
        for (int t : thresholds) {
            if (value >= t) {
                ph++;
            }
        }
        return ph;
    }

    /** 前后值比较：升 → UP，降 → RE，相等 → null。 */
    public static Direction directionFor(long previous, long current) {
        if (current > previous) {
            return Direction.UP;
        }
        if (current < previous) {
            return Direction.RE;
        }
        return null;
    }

    /**
     * 玩家液体槽类型按性别路由（README 1416-1424 玩家 tank 规则）：
     * FEMALE / BOTH → V 型槽（Vfull）；MALE → A 型槽（Afull）；NONE → 无槽。
     */
    public static Slot tankSlotFor(Gender gender) {
        if (gender == null) {
            return null;
        }
        return switch (gender) {
            case MALE -> Slot.AFULL;
            case FEMALE -> Slot.VFULL;
        };
    }
}
