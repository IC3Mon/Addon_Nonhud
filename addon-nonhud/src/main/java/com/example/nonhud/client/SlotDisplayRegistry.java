package com.example.nonhud.client;

import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;

import java.util.EnumMap;
import java.util.Map;

/**
 * 客户端槽位显示注册表：接收 SlotSyncPacket 写人的「槽 → 档位」状态。
 * 纯 Java（不依赖 MC 运行时），模块 4 渲染器按此快照查图集绘制。
 */
public final class SlotDisplayRegistry {

    public static final SlotDisplayRegistry INSTANCE = new SlotDisplayRegistry();

    private volatile Map<Slot, SlotKey> active = Map.of();

    /** 单例供网络接收器使用；包可见构造便于单元测试隔离实例。 */
    SlotDisplayRegistry() { }

    /** 显示某槽某档位（phaseKey 非法则忽略，不影响既有状态）。 */
    public void show(String phaseKey) {
        SlotKey key = SlotKey.fromPhaseKey(phaseKey);
        EnumMap<Slot, SlotKey> next = new EnumMap<>(Slot.class);
        next.putAll(active);
        next.put(key.slot(), key);
        active = Map.copyOf(next);
    }

    /** 隐藏某槽；未知槽名忽略。 */
    public void hide(String slotName) {
        Slot slot;
        try {
            slot = Slot.valueOf(slotName);
        } catch (IllegalArgumentException e) {
            return;
        }
        EnumMap<Slot, SlotKey> next = new EnumMap<>(Slot.class);
        next.putAll(active);
        next.remove(slot);
        active = Map.copyOf(next);
    }

    /** 当前各槽档位快照（不可变）。 */
    public Map<Slot, SlotKey> snapshot() {
        return active;
    }
}
