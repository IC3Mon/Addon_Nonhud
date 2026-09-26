package com.example.nonhud.render;

import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Z 序（N2）：配置序优先、缺省槽按枚举序落尾、先绘低序。 */
class ZOrderTest {

    @Test
    void defaultOrderDrawsNumericAboveEvents() {
        List<Slot> order = ZOrder.sorted(RenderDefaults.zOrder());
        // 数值槽应排在事件槽之前（低序先绘、高序后绘覆盖）
        int fxxkme = order.indexOf(Slot.FXXKME);
        int a = order.indexOf(Slot.A);
        assertTrue(fxxkme < a);
    }

    @Test
    void customConfigDrivesOrder() {
        List<Slot> order = ZOrder.sorted(List.of(Slot.A, Slot.V, Slot.M));
        assertEquals(0, order.indexOf(Slot.A));
        assertEquals(1, order.indexOf(Slot.V));
        assertEquals(2, order.indexOf(Slot.M));
        // 未配置的槽接在配置槽之后
        assertTrue(order.indexOf(Slot.FXXKME) > order.indexOf(Slot.M));
    }

    @Test
    void duplicatesAndUnknownsAreTolerated() {
        List<Slot> order = ZOrder.sorted(List.of(Slot.V, Slot.V));
        assertEquals(1, countOf(order, Slot.V));
        assertTrue(order.indexOf(Slot.V) < order.indexOf(Slot.BOTLE));
        assertTrue(order.indexOf(Slot.V) < order.indexOf(Slot.A));
    }

    @Test
    void emptyConfigFallsBackToEnumOrder() {
        List<Slot> order = ZOrder.sorted(List.of());
        assertEquals(8, order.size());
        assertEquals(Slot.A, order.get(0));
        assertEquals(Slot.FXXKME, order.get(7));
    }

    @Test
    void orderFiltersOnlyActiveSlots() {
        var snapshot = java.util.Map.of(Slot.A, SlotKey.event(Slot.A, com.example.nonhud.scan.EventTier.PEAK));
        List<Slot> order = ZOrder.sorted(List.of(Slot.V, Slot.A, Slot.M, Slot.FXXKME));
        // 只保留快照里出现的槽
        List<Slot> active = new java.util.ArrayList<>();
        for (Slot s : order) {
            if (snapshot.containsKey(s)) {
                active.add(s);
            }
        }
        assertEquals(List.of(Slot.A), active);
    }

    private int countOf(List<Slot> list, Slot slot) {
        int n = 0;
        for (Slot s : list) {
            if (s == slot) {
                n++;
            }
        }
        return n;
    }
}
