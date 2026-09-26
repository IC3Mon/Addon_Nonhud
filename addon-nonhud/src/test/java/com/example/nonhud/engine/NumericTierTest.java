package com.example.nonhud.engine;

import com.example.nonhud.scan.Direction;
import com.example.nonhud.scan.Gender;
import com.example.nonhud.scan.Slot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 数值分档与方向、液体槽性别路由（设计 §3.3 numeric 段）。 */
class NumericTierTest {

    private final List<Integer> energy = List.of(50, 100, 150);

    @Test
    void tierFromValue() {
        assertEquals(1, NumericTier.tierForValue(0, energy));
        assertEquals(1, NumericTier.tierForValue(49, energy));
        assertEquals(2, NumericTier.tierForValue(50, energy));
        assertEquals(2, NumericTier.tierForValue(99, energy));
        assertEquals(3, NumericTier.tierForValue(100, energy));
        assertEquals(4, NumericTier.tierForValue(150, energy));
        assertEquals(4, NumericTier.tierForValue(200, energy));
    }

    @Test
    void emptyThresholdsAlwaysTierOne() {
        assertEquals(1, NumericTier.tierForValue(999, List.of()));
    }

    @Test
    void directionFromValue() {
        assertEquals(Direction.UP, NumericTier.directionFor(10, 20));
        assertEquals(Direction.RE, NumericTier.directionFor(20, 10));
        assertNull(NumericTier.directionFor(15, 15));
    }

    @Test
    void tankSlotByGender() {
        assertEquals(Slot.VFULL, NumericTier.tankSlotFor(Gender.FEMALE));
        assertEquals(Slot.AFULL, NumericTier.tankSlotFor(Gender.MALE));
        assertNull(NumericTier.tankSlotFor(null));
    }
}
