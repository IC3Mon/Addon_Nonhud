package com.example.nonhud.engine;

import com.example.nonhud.scan.FrameSequence;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * 每槽显示状态机（设计文档 §2.2/§3.2）：每个槽位独立维护「当前显示一张」。
 *
 * - show(slot, key, candidates)：进入某档位时从候选帧序列中等概率随机选一个并回到首帧；
 *   档位（key）不变则延续当前序列不重选（避免每帧重选导致闪跳）。
 * - candidates 为空（目录缺图）→ 静默隐藏该槽（设计 §2.3：空目录不播不报错）。
 * - tick：按 fps 推进帧（委托 FrameSequence，fps 语义=每秒帧数）。
 */
public final class TriggerEngine {

    /** 某槽当前显示：档位 key + 选中的帧序列。 */
    public record Displayed(SlotKey key, FrameSequence seq) { }

    private final Map<Slot, Displayed> displayed = new EnumMap<>(Slot.class);
    private final Random random;

    public TriggerEngine() {
        this(new Random());
    }

    public TriggerEngine(Random random) {
        this.random = random;
    }

    /** 显示某槽某档位；候选为空则隐藏；同档位延续不重选。 */
    public void show(Slot slot, SlotKey key, List<FrameSequence> candidates) {
        if (candidates.isEmpty()) {
            hide(slot);
            return;
        }
        Displayed current = displayed.get(slot);
        if (current != null && current.key().equals(key)) {
            return;
        }
        FrameSequence seq = candidates.get(random.nextInt(candidates.size()));
        seq.reset();
        displayed.put(slot, new Displayed(key, seq));
    }

    /** 隐藏某槽（动画结束 / 数值归零等）。 */
    public void hide(Slot slot) {
        displayed.remove(slot);
    }

    /** 推进所有激活槽的帧（每游戏 tick 调用一次）。 */
    public void tick(int ticks) {
        for (Displayed d : displayed.values()) {
            d.seq().advanceTicks(ticks);
        }
    }

    public Optional<Displayed> current(Slot slot) {
        return Optional.ofNullable(displayed.get(slot));
    }

    public boolean isActive(Slot slot) {
        return displayed.containsKey(slot);
    }

    /** 当前各激活槽的档位键（不可变快照）。 */
    public Map<Slot, SlotKey> activeKeys() {
        EnumMap<Slot, SlotKey> keys = new EnumMap<>(Slot.class);
        displayed.forEach((slot, d) -> keys.put(slot, d.key()));
        return Map.copyOf(keys);
    }
}
