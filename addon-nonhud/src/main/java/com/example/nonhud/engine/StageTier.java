package com.example.nonhud.engine;

import com.example.nonhud.scan.EventTier;

/**
 * event 槽峰档计算（设计文档 §3.3）：当前 stage 序号与峰值 stage 序号比较 → pre/peak/post。
 *
 * 峰值 stage 序号由适配层提供：NoN 公共 API 的 NonAnimationStage 不暴露 peak/non_peak 字段
 * （javap 核对结论），按项目约定以「最后一个 stage」近似（CHECKLIST.md §2.7）。
 */
public final class StageTier {

    private StageTier() { }

    /** 峰前/峰/峰后三态判定：小于峰 → PRE；等于峰 → PEAK；大于峰 → POST。 */
    public static EventTier tierForStage(int stageIndex, int peakStageIndex) {
        if (stageIndex < peakStageIndex) {
            return EventTier.PRE;
        }
        if (stageIndex == peakStageIndex) {
            return EventTier.PEAK;
        }
        return EventTier.POST;
    }
}
