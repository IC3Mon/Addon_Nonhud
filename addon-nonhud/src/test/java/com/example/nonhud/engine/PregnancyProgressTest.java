package com.example.nonhud.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 妊娠进度估算（endTick 反推 0–255，CHECKLIST §2.6）。 */
class PregnancyProgressTest {

    @Test
    void firstObservationIsZero() {
        PregnancyProgress progress = new PregnancyProgress();
        assertEquals(0, progress.progress255(10_000));
    }

    @Test
    void progressesTowardBirth() {
        PregnancyProgress progress = new PregnancyProgress();
        progress.progress255(10_000); // 首次观察记 0，同时校准总时长
        assertEquals(64, progress.progress255(7_500));  // 25% 完成 → ~64
        assertEquals(128, progress.progress255(5_000)); // 50% 完成 → ~128
        assertEquals(255, progress.progress255(0));     // 出生 → 255
    }

    @Test
    void clampsBounds() {
        PregnancyProgress progress = new PregnancyProgress();
        progress.progress255(10_000);
        assertEquals(255, progress.progress255(-50));  // 已过 endTick → 255
        assertEquals(0, progress.progress255(20_000)); // 剩余超过估计 → 0
    }

    @Test
    void tinyDurationDoesNotDivideByZero() {
        PregnancyProgress progress = new PregnancyProgress();
        assertEquals(0, progress.progress255(0));
        assertEquals(255, progress.progress255(0));
    }
}
