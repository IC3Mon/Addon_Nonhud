package com.example.nonhud.render;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** 移动方式：FIXED 零偏移、DIRECTION 定向偏移、RANDOM 低频小范围。 */
class MoveModeTest {

    @Test
    void fixedAlwaysZero() {
        MoveMode m = MoveMode.fixed();
        MoveMode.Offset o = m.offset(new Random(1), 999, 0);
        assertEquals(0, o.x());
        assertEquals(0, o.y());
    }

    @Test
    void directionAppliesDxDy() {
        MoveMode m = MoveMode.direction(0, -8);
        MoveMode.Offset o = m.offset(new Random(1), 0, 0);
        assertEquals(0, o.x());
        assertEquals(-8, o.y());
    }

    @Test
    void randomStaysWithinDistance() {
        MoveMode m = MoveMode.random(4, 500);
        Random r = new Random(42);
        for (int i = 0; i < 100; i++) {
            MoveMode.Offset o = m.offset(r, i * 500L, 0);
            assertTrue(Math.abs(o.x()) <= 4, "x out of range: " + o.x());
            assertTrue(Math.abs(o.y()) <= 4, "y out of range: " + o.y());
        }
    }

    @Test
    void randomReRollsPerRefreshPeriod() {
        MoveMode m = MoveMode.random(4, 500);
        Random r = new Random(7);
        MoveMode.Offset t0 = m.offset(r, 0, 0);
        MoveMode.Offset t1 = m.offset(r, 600, 0); // 跨过一个刷新周期
        // 同一周期内稳定、跨周期可能变化（不同周期 seed 不同）
        MoveMode.Offset t0again = m.offset(new Random(7), 0, 0);
        assertEquals(t0, t0again);
        assertNotEquals(t0, t1);
    }
}
