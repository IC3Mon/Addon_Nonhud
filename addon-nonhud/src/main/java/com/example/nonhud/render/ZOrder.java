package com.example.nonhud.render;

import com.example.nonhud.scan.Slot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 槽间 Z 序（叠加优先级，N2）：先绘低序再绘高序，高序覆盖低序。
 * 配置缺省时按 槽位枚举声明序 落尾，保证任何槽都有确定顺序。
 */
public final class ZOrder {

    private ZOrder() { }

    /**
     * 按 zOrder 配置排序槽位：配置中靠前的槽位优先（先绘、被后绘者覆盖）。
     *
     * @param config 配置指定的槽序（靠前 = 低优先级）；可含未知/重复项（容错）
     * @return 槽位顺序（无重复）
     */
    public static List<Slot> sorted(List<Slot> config) {
        Map<Slot, Integer> order = new LinkedHashMap<>();
        int i = 0;
        if (config != null) {
            for (Slot s : config) {
                if (s != null && !order.containsKey(s)) {
                    order.put(s, i++);
                }
            }
        }
        // 未在配置中的槽按枚举序接在末尾（保证任何槽都有确定顺序）
        for (Slot s : Slot.values()) {
            if (!order.containsKey(s)) {
                order.put(s, i++);
            }
        }
        List<Slot> out = new ArrayList<>(order.keySet());
        out.sort((a, b) -> Integer.compare(order.get(a), order.get(b)));
        return out;
    }

    /**
     * 按配置序把快照条目排成绘制序列。
     * @param snapshot 当前激活槽快照（槽 → 档位）
     * @param config   配置槽序
     */
    public static List<Map.Entry<Slot, ?>> order(Map<Slot, ?> snapshot, List<Slot> config) {
        List<Slot> ordered = sorted(config);
        List<Map.Entry<Slot, ?>> out = new ArrayList<>();
        for (Slot s : ordered) {
            if (snapshot.containsKey(s)) {
                out.add(snapshot.entrySet().stream()
                        .filter(e -> e.getKey() == s)
                        .findFirst()
                        .orElseThrow());
            }
        }
        return out;
    }

    /** 默认 Z 序（设计文档 §6 草案）：数值槽在上、事件槽在下。 */
    public static List<Slot> defaults() {
        EnumMap<Slot, Integer> order = new EnumMap<>(Slot.class);
        order.put(Slot.FXXKME, 0);
        order.put(Slot.PREGNANT, 1);
        order.put(Slot.AFULL, 2);
        order.put(Slot.VFULL, 3);
        order.put(Slot.BOTLE, 4);
        order.put(Slot.M, 5);
        order.put(Slot.V, 6);
        order.put(Slot.A, 7);
        List<Slot> out = new ArrayList<>(order.keySet());
        out.sort((a, b) -> Integer.compare(order.get(a), order.get(b)));
        return out;
    }
}
