package com.example.nonhud.engine;

import com.example.nonhud.scan.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 数值槽滞回：首采触发、穿档触发、小幅波动不触发、方向携带。 */
class NumericTrackerTest {

    private final List<Integer> thresholds = List.of(30, 60, 100);

    @Test
    void firstSampleFires() {
        NumericTracker tracker = new NumericTracker();
        Optional<NumericTracker.Change> change = tracker.update(80, thresholds);
        assertTrue(change.isPresent());
        assertEquals(3, change.get().ph());
        assertEquals(null, change.get().direction());
    }

    @Test
    void sameValueDoesNotFire() {
        NumericTracker tracker = new NumericTracker();
        tracker.update(80, thresholds);
        assertFalse(tracker.update(80, thresholds).isPresent());
    }

    @Test
    void smallFluctuationWithinTierDoesNotFire() {
        NumericTracker tracker = new NumericTracker();
        tracker.update(80, thresholds);
        assertFalse(tracker.update(85, thresholds).isPresent());
    }

    @Test
    void crossingUpFiresWithDirectionUp() {
        NumericTracker tracker = new NumericTracker();
        tracker.update(80, thresholds);
        Optional<NumericTracker.Change> change = tracker.update(120, thresholds);
        assertTrue(change.isPresent());
        assertEquals(4, change.get().ph());
        assertEquals(Direction.UP, change.get().direction());
    }

    @Test
    void crossingDownFiresWithDirectionRe() {
        NumericTracker tracker = new NumericTracker();
        tracker.update(120, thresholds);
        Optional<NumericTracker.Change> change = tracker.update(50, thresholds);
        assertTrue(change.isPresent());
        assertEquals(2, change.get().ph());
        assertEquals(Direction.RE, change.get().direction());
    }

    @Test
    void consecutiveUpCrossings() {
        NumericTracker tracker = new NumericTracker();
        tracker.update(20, thresholds);   // ph1
        assertEquals(2, tracker.update(50, thresholds).orElseThrow().ph());
        assertEquals(3, tracker.update(80, thresholds).orElseThrow().ph());
        assertEquals(4, tracker.update(150, thresholds).orElseThrow().ph());
    }
}
