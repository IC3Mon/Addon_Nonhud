package com.example.nonhud.engine;

import com.example.nonhud.scan.FrameSequence;
import com.example.nonhud.scan.Gender;
import com.example.nonhud.scan.EventTier;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 每槽显示状态机：重选语义、同档延续、空候选静默、帧推进、独立槽位。 */
class TriggerEngineTest {

    private final Path f1 = Path.of("f01.png");
    private final Path f2 = Path.of("f02.png");
    private final Path f3 = Path.of("f03.png");

    private final SlotKey preKey = SlotKey.event(Slot.V, EventTier.PRE);
    private final SlotKey peakKey = SlotKey.event(Slot.V, EventTier.PEAK);
    private final List<FrameSequence> seqA = List.of(
            FrameSequence.sequence("A", List.of(f1, f2, f3), 2.0));
    private final List<FrameSequence> seqB = List.of(
            FrameSequence.sequence("B", List.of(f1, f2), 2.0));

    @Test
    void showMakesSlotActiveWithPickedSequence() {
        TriggerEngine engine = new TriggerEngine(new Random(0));
        engine.show(Slot.V, preKey, seqA);
        Optional<TriggerEngine.Displayed> current = engine.current(Slot.V);
        assertTrue(current.isPresent());
        assertEquals(preKey, current.get().key());
        assertEquals(f1, current.get().seq().current());
    }

    @Test
    void sameKeyContinuesWithoutReselect() {
        TriggerEngine engine = new TriggerEngine(new Random(0));
        engine.show(Slot.V, preKey, seqA);
        FrameSequence first = engine.current(Slot.V).orElseThrow().seq();
        engine.tick(10); // 推进 1 帧 → f2
        engine.show(Slot.V, preKey, seqA); // 同档位再次进入：不重选
        assertEquals(first, engine.current(Slot.V).orElseThrow().seq());
        assertEquals(f2, first.current());
    }

    @Test
    void tierChangeReselectsSequence() {
        TriggerEngine engine = new TriggerEngine(new Random(0));
        engine.show(Slot.V, preKey, seqA);
        engine.show(Slot.V, peakKey, seqB);
        TriggerEngine.Displayed current = engine.current(Slot.V).orElseThrow();
        assertEquals(peakKey, current.key());
        assertEquals(f1, current.seq().current()); // 重选后回到首帧
    }

    @Test
    void emptyCandidatesHidesSlot() {
        TriggerEngine engine = new TriggerEngine(new Random(0));
        engine.show(Slot.V, preKey, seqA);
        engine.show(Slot.V, peakKey, List.of());
        assertFalse(engine.isActive(Slot.V));
    }

    @Test
    void hideRemovesSlot() {
        TriggerEngine engine = new TriggerEngine(new Random(0));
        engine.show(Slot.V, preKey, seqA);
        engine.hide(Slot.V);
        assertFalse(engine.isActive(Slot.V));
    }

    @Test
    void tickAdvancesFramesPerFps() {
        TriggerEngine engine = new TriggerEngine(new Random(0));
        engine.show(Slot.V, preKey, seqA);
        engine.tick(10); // 0.5 秒 @2fps → 1 帧
        assertEquals(f2, engine.current(Slot.V).orElseThrow().seq().current());
        engine.tick(10); // 再 1 帧
        assertEquals(f3, engine.current(Slot.V).orElseThrow().seq().current());
    }

    @Test
    void slotsAreIndependent() {
        TriggerEngine engine = new TriggerEngine(new Random(0));
        SlotKey aKey = SlotKey.eventA(Gender.FEMALE, EventTier.PEAK);
        engine.show(Slot.V, preKey, seqA);
        engine.show(Slot.A, aKey, seqB);
        engine.hide(Slot.V);
        assertFalse(engine.isActive(Slot.V));
        assertTrue(engine.isActive(Slot.A));
        assertEquals(aKey, engine.current(Slot.A).orElseThrow().key());
    }

    @Test
    void activeKeysSnapshot() {
        TriggerEngine engine = new TriggerEngine(new Random(0));
        SlotKey aKey = SlotKey.eventA(Gender.FEMALE, EventTier.PEAK);
        engine.show(Slot.V, preKey, seqA);
        engine.show(Slot.A, aKey, seqB);
        Map<Slot, SlotKey> keys = engine.activeKeys();
        assertEquals(Map.of(Slot.V, preKey, Slot.A, aKey), keys);
        // 快照不可变
        assertThrows(UnsupportedOperationException.class,
                () -> keys.put(Slot.M, peakKey));
    }
}
