package com.example.nonhud.render;

import java.util.List;
import java.util.Random;

/**
 * 组内重叠错开（模块 5.1）：组开启重叠且同组多槽图片重叠超过允许比例时，
 * 将后放置的图片在组 area 内平移错开，防止完全重叠。
 *
 * 重叠比例 = 两矩形相交面积 ÷ 较小者面积 × 100。
 * 候选方向：右 / 下 / 左 / 上，逐步平移（步长 = 高度/4 与 宽度/4 取小，至少 2px）；
 * 取第一个满足「与所有已放置图片重叠 ≤ 允许比例」且在 area 内的候选；
 * 无满足候选时保底返回重叠最小的候选。
 */
public final class OverlapPlacer {

    private OverlapPlacer() { }

    /**
     * 为单个绘制矩形计算错开后的位置。
     *
     * @param group  所属组（提供 area 与 overlapPercent）
     * @param raw    原始绘制矩形（已按槽位定位并约束到 area）
     * @param placed 同组内已放置的绘制矩形（按其绘制顺序）
     * @param random 随机源（无方向偏好时随机选方向起点）
     */
    public static SlotRenderState.Draw place(Group group, SlotRenderState.Draw raw,
                                             List<SlotRenderState.Draw> placed, Random random) {
        if (!group.overlapEnabled() || placed.isEmpty() || !raw.visible()) {
            return raw;
        }
        int maxPct = group.overlapPercent();
        if (placed.stream().allMatch(p -> overlapPercent(raw, p) <= maxPct)) {
            return raw;
        }
        Region area = group.area();
        int step = Math.max(2, Math.min(raw.width(), raw.height()) / 4);
        int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}; // 右/下/左/上
        // 随机方向起点（方向本身无偏好，随机起点避免恒定偏向一侧）
        int start = random.nextInt(dirs.length);
        SlotRenderState.Draw best = raw;
        double bestOverlap = Double.MAX_VALUE;
        for (int attempt = 0; attempt < 40; attempt++) {
            int[] dir = dirs[(start + attempt) % dirs.length];
            SlotRenderState.Draw cand = shift(raw, dir[0] * step, dir[1] * step);
            if (!inside(cand, area)) {
                continue;
            }
            double worst = placed.stream()
                    .mapToDouble(p -> overlapPercent(cand, p))
                    .max().orElse(0);
            if (worst < bestOverlap) {
                bestOverlap = worst;
                best = cand;
            }
            if (worst <= maxPct) {
                return cand;
            }
        }
        return best;
    }

    /** 两矩形重叠面积占较小者面积的比例（0-100）。 */
    public static double overlapPercent(SlotRenderState.Draw a, SlotRenderState.Draw b) {
        int iw = Math.min(a.x() + a.width(), b.x() + b.width()) - Math.max(a.x(), b.x());
        int ih = Math.min(a.y() + a.height(), b.y() + b.height()) - Math.max(a.y(), b.y());
        if (iw <= 0 || ih <= 0) {
            return 0;
        }
        int inter = iw * ih;
        int min = Math.min(a.width() * a.height(), b.width() * b.height());
        return min <= 0 ? 0 : inter * 100.0 / min;
    }

    private static SlotRenderState.Draw shift(SlotRenderState.Draw d, int dx, int dy) {
        return new SlotRenderState.Draw(d.x() + dx, d.y() + dy, d.width(), d.height(),
                d.alpha(), d.framePath(), d.visible());
    }

    private static boolean inside(SlotRenderState.Draw d, Region area) {
        return d.x() >= area.x() && d.y() >= area.y()
                && d.x() + d.width() <= area.x() + area.width()
                && d.y() + d.height() <= area.y() + area.height();
    }
}
