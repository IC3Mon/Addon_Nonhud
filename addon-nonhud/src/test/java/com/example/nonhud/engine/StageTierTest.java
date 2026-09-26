package com.example.nonhud.engine;

import com.example.nonhud.scan.EventTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 峰档判定（设计 §3.3）：stage 序号 vs 峰值序号 → pre/peak/post。 */
class StageTierTest {

    @Test
    void beforePeakIsPre() {
        assertEquals(EventTier.PRE, StageTier.tierForStage(0, 2));
        assertEquals(EventTier.PRE, StageTier.tierForStage(1, 2));
    }

    @Test
    void atPeakIsPeak() {
        assertEquals(EventTier.PEAK, StageTier.tierForStage(2, 2));
        // 峰值在首段（0 号）：第 0 段即为峰
        assertEquals(EventTier.PEAK, StageTier.tierForStage(0, 0));
    }

    @Test
    void afterPeakIsPost() {
        assertEquals(EventTier.POST, StageTier.tierForStage(3, 2));
        assertEquals(EventTier.POST, StageTier.tierForStage(1, 0));
    }

    @Test
    void lastStageAsPeakApproximation() {
        // 峰值近似 = 末段（CHECKLIST §2.7）：除末段外均为峰前
        assertEquals(EventTier.PRE, StageTier.tierForStage(0, 3));
        assertEquals(EventTier.PRE, StageTier.tierForStage(2, 3));
        assertEquals(EventTier.PEAK, StageTier.tierForStage(3, 3));
    }
}
