package com.example.nonhud.scan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模块 0 索引缓存：槽位+阶段档位 → 候选帧序列列表。
 *
 * 不可变共享数据（O1）：扫描重建后整体替换引用，避免并发写。
 * 同「槽位+阶段」下多个前缀构成候选列表，进入该阶段时随机选一个播放（设计 §2.2）。
 */
public final class SlotIndex {

    private final Map<SlotKey, List<FrameSequence>> index;

    private SlotIndex(Map<SlotKey, List<FrameSequence>> index) {
        this.index = index;
    }

    public static SlotIndex of(Map<SlotKey, List<FrameSequence>> index) {
        Map<SlotKey, List<FrameSequence>> copy = new HashMap<>();
        index.forEach((k, v) -> copy.put(k, List.copyOf(v)));
        return new SlotIndex(Collections.unmodifiableMap(copy));
    }

    /** 该键下的候选帧序列列表；无则返回空列表（静默不播）。 */
    public List<FrameSequence> get(SlotKey key) {
        return index.getOrDefault(key, List.of());
    }

    /** 索引中是否含该槽（任意档位）。 */
    public boolean has(Slot slot) {
        for (SlotKey k : index.keySet()) {
            if (k.slot() == slot) {
                return true;
            }
        }
        return false;
    }

    /** 索引中出现的所有槽位。 */
    public Set<Slot> slots() {
        EnumSet<Slot> slots = EnumSet.noneOf(Slot.class);
        for (SlotKey k : index.keySet()) {
            slots.add(k.slot());
        }
        return slots;
    }

    /** 已索引的（槽位+档位）键数量。 */
    public int size() {
        return index.size();
    }
}
