package com.example.nonhud.render;

import com.example.nonhud.scan.EventTier;
import com.example.nonhud.scan.FrameSequence;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** 模块 5.1 分组模型：组校验 / 多组多份 / 重叠错开 / 组 fps 与 area 约束。 */
class GroupOverlapTest {

    private final Path f1 = Path.of("f01.png");
    private final Path f2 = Path.of("f02.png");
    private final Path f3 = Path.of("f03.png");

    @Test
    void groupValidationClampsBadValuesAndRejects() {
        Group g = new Group("g1", List.of(Slot.A), new Region(0, 0, 100, 100),
                0.05, -10, -10, true, 150, null);
        assertEquals(0.1, g.fps(), 1e-6);      // fps 下限钳制
        assertEquals(0, g.fadeInMs());
        assertEquals(0, g.fadeOutMs());
        assertEquals(100, g.overlapPercent()); // 上限钳制
        assertEquals(Group.Layer.BACK, g.layer()); // null → BACK
        assertTrue(g.contains(Slot.A));
        assertFalse(g.contains(Slot.V));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("", List.of(Slot.A), new Region(0, 0, 1, 1), 1, 0, 0, false, 0, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("g1", List.of(Slot.A), null, 1, 0, 0, false, 0, null));
    }

    @Test
    void slotCanBelongToMultipleGroups() {
        RenderDefaults.setGroups(List.of(
                new Group("g1", List.of(Slot.A, Slot.V), new Region(0, 0, 100, 100), 1, 0, 0, false, 0, Group.Layer.BACK),
                new Group("g2", List.of(Slot.A), new Region(0, 100, 100, 100), 1, 0, 0, false, 0, Group.Layer.FRONT)));
        // A 同时属于 g1/g2 → 两处都显示
        assertEquals(2, RenderDefaults.groupsOf(Slot.A).size());
        assertEquals(1, RenderDefaults.groupsOf(Slot.V).size());
        // M 不在任何组 → 回退默认第一组（不空）
        assertFalse(RenderDefaults.groupsOf(Slot.M).isEmpty());
        RenderDefaults.setGroups(RenderDefaults.defaultGroups());
    }

    @Test
    void overlapPercentComputesProportion() {
        SlotRenderState.Draw a = new SlotRenderState.Draw(0, 0, 100, 100, 1f, f1, true);
        SlotRenderState.Draw disjoint = new SlotRenderState.Draw(200, 200, 100, 100, 1f, f1, true);
        SlotRenderState.Draw identical = new SlotRenderState.Draw(0, 0, 100, 100, 1f, f1, true);
        SlotRenderState.Draw half = new SlotRenderState.Draw(50, 0, 100, 100, 1f, f1, true);
        assertEquals(0, OverlapPlacer.overlapPercent(a, disjoint), 1e-6);
        assertEquals(100, OverlapPlacer.overlapPercent(a, identical), 1e-6);
        // 相交 50x100=5000 / 较小者 100x100=10000 → 50%
        assertEquals(50, OverlapPlacer.overlapPercent(a, half), 1e-6);
    }

    @Test
    void overlapPlacerShiftsFullyOverlappedDrawInsideArea() {
        Group g = new Group("g1", List.of(Slot.A, Slot.V), new Region(0, 0, 200, 200),
                1, 0, 0, true, 20, Group.Layer.BACK);
        SlotRenderState.Draw first = new SlotRenderState.Draw(50, 50, 100, 100, 1f, f1, true);
        SlotRenderState.Draw second = new SlotRenderState.Draw(50, 50, 100, 100, 1f, f2, true);
        SlotRenderState.Draw placed = OverlapPlacer.place(g, second, List.of(first), new Random(1));

        // 防止完全重叠：错开后与首图不再完全相同（重叠 < 100%）
        assertNotEquals(first, placed);
        assertTrue(OverlapPlacer.overlapPercent(first, placed) < 100);
        // 候选始终在 area 内
        assertTrue(placed.x() >= 0 && placed.y() >= 0);
        assertTrue(placed.x() + placed.width() <= 200 && placed.y() + placed.height() <= 200);
    }

    @Test
    void overlapPlacerReturnsRawWhenWithinLimit() {
        Group g = new Group("g1", List.of(Slot.A, Slot.V), new Region(0, 0, 200, 200),
                1, 0, 0, true, 80, Group.Layer.BACK);
        SlotRenderState.Draw a = new SlotRenderState.Draw(0, 0, 100, 100, 1f, f1, true);
        SlotRenderState.Draw b = new SlotRenderState.Draw(20, 20, 100, 100, 1f, f2, true);
        // 相交 80x80=6400 / 10000 = 64% ≤ 80 → 原样返回
        SlotRenderState.Draw placed = OverlapPlacer.place(g, b, List.of(a), new Random(1));
        assertEquals(b, placed);
    }

    @Test
    void overlapDisabledReturnsRaw() {
        Group g = new Group("g1", List.of(Slot.A, Slot.V), new Region(0, 0, 200, 200),
                1, 0, 0, false, 0, Group.Layer.BACK);
        SlotRenderState.Draw a = new SlotRenderState.Draw(0, 0, 100, 100, 1f, f1, true);
        SlotRenderState.Draw b = new SlotRenderState.Draw(0, 0, 100, 100, 1f, f2, true);
        assertEquals(b, OverlapPlacer.place(g, b, List.of(a), new Random(1)));
    }

    @Test
    void slotStateUsesGroupFpsAndConstrainsToArea() {
        Group g = new Group("g1", List.of(Slot.A), new Region(0, 0, 50, 50), 4.0, 0, 0, false, 0, Group.Layer.BACK);
        FrameSequence seq = FrameSequence.sequence("A1", List.of(f1, f2, f3), 2.0);
        SlotRenderState s = new SlotRenderState(Slot.A, SlotKey.event(Slot.A, EventTier.PEAK), seq,
                new Region(0, 0, 100, 100), MoveMode.fixed(), ScaleMode.FIT, 1.0,
                g, 0, 0, new Random(1));
        // 组 fps=4：1 秒推进 4 帧 → (0+4)%3 = 1 → 第 2 帧
        s.advance(1000);
        assertEquals(f2, s.currentFramePath());
        // 图片 100x100 FIT 进 region 100x100，再受组 area 50x50 约束 → 缩到 50x50 居中
        SlotRenderState.Draw d = s.draw(100, 100);
        assertEquals(50, d.width());
        assertEquals(50, d.height());
        assertEquals(0, d.x());
        assertEquals(0, d.y());
    }

    @Test
    void slotStateWithoutGroupUsesSequenceFps() {
        FrameSequence seq = FrameSequence.sequence("A1", List.of(f1, f2, f3), 2.0);
        SlotRenderState s = new SlotRenderState(Slot.A, SlotKey.event(Slot.A, EventTier.PEAK), seq,
                new Region(0, 0, 100, 100), MoveMode.fixed(), ScaleMode.FIT, 1.0,
                0, 0, new Random(1));
        s.advance(1000); // 序列 fps=2 → 2 帧
        assertEquals(f3, s.currentFramePath());
    }
}
