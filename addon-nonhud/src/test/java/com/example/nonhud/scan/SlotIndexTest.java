package com.example.nonhud.scan;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 槽索引：不可变共享数据（O1 重建后引用替换）、未知键返回空、槽位集合。 */
class SlotIndexTest {

    private final Path f1 = Path.of("f01.png");

    private Map<SlotKey, List<FrameSequence>> source() {
        Map<SlotKey, List<FrameSequence>> map = new HashMap<>();
        map.put(SlotKey.event(Slot.V, EventTier.PRE),
                new ArrayList<>(List.of(FrameSequence.sequence("A1", List.of(f1), 2.0))));
        map.put(SlotKey.botle(), new ArrayList<>(List.of(FrameSequence.single("botle", f1))));
        return map;
    }

    @Test
    void getReturnsCandidatesForKey() {
        SlotIndex idx = SlotIndex.of(source());
        assertEquals(1, idx.get(SlotKey.event(Slot.V, EventTier.PRE)).size());
        assertEquals("A1", idx.get(SlotKey.event(Slot.V, EventTier.PRE)).get(0).prefix());
    }

    @Test
    void unknownKeyReturnsEmptyList() {
        SlotIndex idx = SlotIndex.of(source());
        assertTrue(idx.get(SlotKey.event(Slot.V, EventTier.POST)).isEmpty());
        assertTrue(idx.get(SlotKey.fxxkme(1)).isEmpty());
    }

    @Test
    void hasAndSlots() {
        SlotIndex idx = SlotIndex.of(source());
        assertTrue(idx.has(Slot.V));
        assertTrue(idx.has(Slot.BOTLE));
        assertFalse(idx.has(Slot.M));
        assertEquals(2, idx.slots().size());
    }

    @Test
    void indexIsImmutableSnapshot() {
        Map<SlotKey, List<FrameSequence>> map = source();
        SlotIndex idx = SlotIndex.of(map);

        // 修改源 map 不影响索引
        map.put(SlotKey.event(Slot.M, EventTier.PRE), List.of(FrameSequence.single("x", f1)));
        assertFalse(idx.has(Slot.M));

        // 修改源列表不影响索引
        map.get(SlotKey.botle()).add(FrameSequence.single("y", f1));
        assertEquals(1, idx.get(SlotKey.botle()).size());

        // 索引自身列表不可变
        assertThrows(UnsupportedOperationException.class,
                () -> idx.get(SlotKey.botle()).add(FrameSequence.single("z", f1)));
    }
}
