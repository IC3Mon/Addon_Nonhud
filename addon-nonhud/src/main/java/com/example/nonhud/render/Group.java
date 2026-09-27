package com.example.nonhud.render;

import com.example.nonhud.scan.Slot;

import java.util.List;

/**
 * 显示组（模块 5.1 分组重构）：一组槽共享一个 area（生成位置 + 边界约束）。
 *
 * 字段语义（与用户确认）：
 * - area：图片生成位置基准 + 边界约束，图片不能超出 area；
 * - 同组多槽保留各自 region 作为组内相对偏移，落入组 area；
 * - fps / 淡入 / 淡出按组独立设置；
 * - overlapEnabled：组内多槽图片是否允许互相重叠；
 * - overlapPercent：允许的最大重叠比例（0-100），超限自动错开防止完全重叠；
 * - layer：组与组之间的绘制层级，FRONT 在上层（后绘制），BACK 在下层（先绘制）。
 */
public record Group(
        String id,
        List<Slot> slots,
        Region area,
        double fps,
        long fadeInMs,
        long fadeOutMs,
        boolean overlapEnabled,
        int overlapPercent,
        Layer layer) {

    /** 组绘制层级：FRONT=在前（上层），BACK=在后（下层）。 */
    public enum Layer { FRONT, BACK }

    public Group {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Group id must not be blank");
        }
        if (area == null) {
            throw new IllegalArgumentException("Group area must not be null");
        }
        fps = Math.max(0.1, fps);
        fadeInMs = Math.max(0, fadeInMs);
        fadeOutMs = Math.max(0, fadeOutMs);
        overlapPercent = Math.max(0, Math.min(100, overlapPercent));
        layer = layer == null ? Layer.BACK : layer;
    }

    public boolean contains(Slot slot) {
        return slots != null && slots.contains(slot);
    }
}
