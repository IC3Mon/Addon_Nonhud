package com.example.nonhud.client;

import com.example.nonhud.scan.Direction;
import com.example.nonhud.scan.EventTier;
import com.example.nonhud.scan.Gender;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 客户端槽位显示注册表：show/hide/snapshot（模块 4 渲染器消费）。 */
class SlotDisplayRegistryTest {

    @Test
    void showAddsSlot() {
        SlotDisplayRegistry registry = new SlotDisplayRegistry();
        registry.show("V|PEAK");
        assertEquals(Map.of(Slot.V, SlotKey.event(Slot.V, EventTier.PEAK)),
                registry.snapshot());
    }

    @Test
    void showReplacesSameSlot() {
        SlotDisplayRegistry registry = new SlotDisplayRegistry();
        registry.show("V|PRE");
        registry.show("V|PEAK");
        assertEquals(Map.of(Slot.V, SlotKey.event(Slot.V, EventTier.PEAK)),
                registry.snapshot());
    }

    @Test
    void hideRemovesSlotOnly() {
        SlotDisplayRegistry registry = new SlotDisplayRegistry();
        registry.show("A|FEMALE|PEAK");
        registry.show("FXXKME|3");
        registry.hide("A");
        assertEquals(Map.of(Slot.FXXKME, SlotKey.fxxkme(3)), registry.snapshot());
    }

    @Test
    void unknownSlotHideIgnored() {
        SlotDisplayRegistry registry = new SlotDisplayRegistry();
        registry.show("BOTLE|SINGLE");
        registry.hide("NOT_A_SLOT");
        assertTrue(registry.snapshot().containsKey(Slot.BOTLE));
    }

    @Test
    void snapshotIsImmutable() {
        SlotDisplayRegistry registry = new SlotDisplayRegistry();
        registry.show("VFULL|UP|2");
        Map<Slot, SlotKey> snapshot = registry.snapshot();
        try {
            snapshot.put(Slot.M, SlotKey.event(Slot.M, EventTier.SINGLE));
            throw new AssertionError("snapshot must be immutable");
        } catch (UnsupportedOperationException expected) {
            // ok
        }
        assertEquals(SlotKey.numeric(Slot.VFULL, Direction.UP, 2),
                registry.snapshot().get(Slot.VFULL));
    }

    @Test
    void genderSlotRoundTripsThroughPhaseKey() {
        SlotDisplayRegistry registry = new SlotDisplayRegistry();
        registry.show(SlotKey.eventA(Gender.MALE, EventTier.POST).phaseKey());
        assertEquals(SlotKey.eventA(Gender.MALE, EventTier.POST),
                registry.snapshot().get(Slot.A));
    }
}
