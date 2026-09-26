package com.example.nonhud.engine;

/**
 * 妊娠进度估算（CHECKLIST.md §2.6/§5#6）：公共 API 的 NonPregnancySnapshot 只有 endTick，
 * 无进度百分比。以首次观察到的剩余 tick 作为总时长估计，之后由
 * progress = 255 × (1 − 剩余/总时长) 反推 0–255 进度（与 NoN 的 needsofnature_pregnancy_progress 同量纲）。
 */
public final class PregnancyProgress {

    private long startRemainingTicks;
    private boolean initialized;

    /** 由剩余 tick 估算 0–255 进度；首次观察记为 0；结果夹在 [0,255]。 */
    public int progress255(long remainingTicks) {
        if (!initialized) {
            startRemainingTicks = Math.max(1, remainingTicks);
            initialized = true;
            return 0;
        }
        double fraction = 1.0 - ((double) remainingTicks / startRemainingTicks);
        int progress = (int) Math.round(255.0 * fraction);
        return Math.max(0, Math.min(255, progress));
    }
}
