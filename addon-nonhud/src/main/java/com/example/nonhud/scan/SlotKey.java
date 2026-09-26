package com.example.nonhud.scan;

import java.util.StringJoiner;

/**
 * 槽索引键：槽位 + 阶段档位。
 *
 * 各字段取值（其余为 null/0）：
 * - event/A：slot=A, gender, tier（pre/peak/post）
 * - event/V|M：slot, tier
 * - botle：slot=BOTLE, tier=SINGLE
 * - numeric Vfull/Afull：slot, direction(up/re), ph
 * - numeric fxxkme：slot, ph
 * - numeric pregnant：slot, ph；出生图集 birth：slot=PREGNANT, tier=SINGLE
 */
public record SlotKey(Slot slot, Gender gender, Direction direction, EventTier tier, int ph) {

    /** 序列化为 S2C 可传输的相位键：slot|gender|direction|tier|ph（省略 null/0 段）。 */
    public String phaseKey() {
        StringJoiner joiner = new StringJoiner("|");
        joiner.add(slot.name());
        if (gender != null) {
            joiner.add(gender.name());
        }
        if (direction != null) {
            joiner.add(direction.name());
        }
        if (tier != null) {
            joiner.add(tier.name());
        }
        if (ph > 0) {
            joiner.add(Integer.toString(ph));
        }
        return joiner.toString();
    }

    /** phaseKey() 的反向解析；槽位名非法抛 IllegalArgumentException。 */
    public static SlotKey fromPhaseKey(String key) {
        String[] parts = key.split("\\|", -1);
        Slot slot = Slot.valueOf(parts[0]);
        Gender gender = null;
        Direction direction = null;
        EventTier tier = null;
        int ph = 0;
        for (int i = 1; i < parts.length; i++) {
            String t = parts[i];
            if (gender == null) {
                Gender g = tryGender(t);
                if (g != null) {
                    gender = g;
                    continue;
                }
            }
            if (direction == null) {
                Direction d = tryDirection(t);
                if (d != null) {
                    direction = d;
                    continue;
                }
            }
            if (tier == null) {
                EventTier e = tryTier(t);
                if (e != null) {
                    tier = e;
                    continue;
                }
            }
            ph = Integer.parseInt(t);
        }
        return new SlotKey(slot, gender, direction, tier, ph);
    }

    private static Gender tryGender(String s) {
        try {
            return Gender.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Direction tryDirection(String s) {
        try {
            return Direction.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static EventTier tryTier(String s) {
        try {
            return EventTier.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static SlotKey eventA(Gender gender, EventTier tier) {
        return new SlotKey(Slot.A, gender, null, tier, 0);
    }

    /** event/V、event/M（无性别级）。 */
    public static SlotKey event(Slot slot, EventTier tier) {
        return new SlotKey(slot, null, null, tier, 0);
    }

    /** event/botle（单档）。 */
    public static SlotKey botle() {
        return new SlotKey(Slot.BOTLE, null, null, EventTier.SINGLE, 0);
    }

    /** numeric/Vfull、numeric/Afull（带方向）。 */
    public static SlotKey numeric(Slot slot, Direction direction, int ph) {
        return new SlotKey(slot, null, direction, null, ph);
    }

    /** numeric/fxxkme（无方向，按 energy 分档）。 */
    public static SlotKey fxxkme(int ph) {
        return new SlotKey(Slot.FXXKME, null, null, null, ph);
    }

    /** numeric/pregnant（按进度分档）。 */
    public static SlotKey pregnant(int ph) {
        return new SlotKey(Slot.PREGNANT, null, null, null, ph);
    }

    /** numeric/pregnant/birth（出生图集，单档）。 */
    public static SlotKey birth() {
        return new SlotKey(Slot.PREGNANT, null, null, EventTier.SINGLE, 0);
    }
}
