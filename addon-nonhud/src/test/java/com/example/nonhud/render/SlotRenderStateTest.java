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

/** 渲染实例状态机：淡入淡出、帧推进、缩放/拉伸、移动偏移。 */
class SlotRenderStateTest {

    private final SlotKey key = SlotKey.event(Slot.V, EventTier.PEAK);
    private final Path f1 = Path.of("f01.png");
    private final Path f2 = Path.of("f02.png");
    private final Path f3 = Path.of("f03.png");

    private SlotRenderState state(Region region, MoveMode move, ScaleMode scale,
                                  FrameSequence seq, long fadeIn, long fadeOut) {
        return new SlotRenderState(Slot.V, key, seq, region, move, scale, 1.0, fadeIn, fadeOut, new Random(1));
    }

    @Test
    void fadesInFromZeroToFull() {
        SlotRenderState s = state(new Region(0, 0, 90, 90), MoveMode.fixed(), ScaleMode.FIT,
                FrameSequence.single("x", f1), 300, 300);
        assertEquals(0f, s.alpha());
        s.advance(150);
        assertEquals(0.5f, s.alpha(), 0.001f);
        s.advance(150);
        assertEquals(1f, s.alpha(), 0.001f);
        assertTrue(s.active());
    }

    @Test
    void advancesFramesByFpsOverTime() {
        // fps=2 每秒 2 帧：1000ms 推进 2 帧
        FrameSequence seq = FrameSequence.sequence("x", List.of(f1, f2, f3), 2.0);
        SlotRenderState s = state(new Region(0, 0, 90, 90), MoveMode.fixed(), ScaleMode.FIT, seq, 0, 0);
        s.advance(1000); // 0 → (0+2)%3 = 2
        assertEquals(f3, s.currentFramePath());
        s.advance(1000); // 2 → (2+2)%3 = 1
        assertEquals(f2, s.currentFramePath());
        s.advance(1500); // 1 → (1+3)%3 = 1（循环一周）
        assertEquals(f2, s.currentFramePath());
    }

    @Test
    void singleImageNeverAdvances() {
        SlotRenderState s = state(new Region(0, 0, 90, 90), MoveMode.fixed(), ScaleMode.FIT,
                FrameSequence.single("myimg", f1), 0, 0);
        s.advance(100_000);
        assertEquals(f1, s.currentFramePath());
    }

    @Test
    void fadeOutReturnsToZeroAndRemovable() {
        SlotRenderState s = state(new Region(0, 0, 90, 90), MoveMode.fixed(), ScaleMode.FIT,
                FrameSequence.single("x", f1), 0, 200);
        s.advance(100); // alpha=1（fadeIn=0 直接全亮）
        assertEquals(1f, s.alpha());
        s.deactivate();
        s.advance(100);
        assertEquals(0.5f, s.alpha(), 0.001f);
        s.advance(100);
        assertEquals(0f, s.alpha(), 0.001f);
        assertFalse(s.active());
    }

    @Test
    void drawFitScalesInsideRegionCentered() {
        // 区域 100x50，图片 200x100 → scale=0.5 → 100x50（恰好填满区域），居中于 (10, 20)
        SlotRenderState s = state(new Region(10, 20, 100, 50), MoveMode.fixed(), ScaleMode.FIT,
                FrameSequence.single("x", f1), 0, 0);
        s.advance(100);
        SlotRenderState.Draw d = s.draw(200, 100);
        assertEquals(10, d.x());
        assertEquals(20, d.y());
        assertEquals(100, d.width());
        assertEquals(50, d.height());

        // 区域 100x50，图片 100x200 → scale=0.25 → 25x50，水平居中于 (10+37, 20)
        SlotRenderState s2 = state(new Region(10, 20, 100, 50), MoveMode.fixed(), ScaleMode.FIT,
                FrameSequence.single("x", f1), 0, 0);
        s2.advance(100);
        SlotRenderState.Draw d2 = s2.draw(100, 200);
        assertEquals(47, d2.x());
        assertEquals(20, d2.y());
        assertEquals(25, d2.width());
        assertEquals(50, d2.height());
    }

    @Test
    void drawAppliesScalePercent() {
        // 区域 100x50，图片 200x100 → FIT 100x50；scalePercent=2.0 → 200x100（越界但以区域中心对齐）
        SlotRenderState s = new SlotRenderState(Slot.V, key, FrameSequence.single("x", f1),
                new Region(10, 20, 100, 50), MoveMode.fixed(), ScaleMode.FIT, 2.0, 0, 0, new Random(1));
        s.advance(100);
        SlotRenderState.Draw d = s.draw(200, 100);
        assertEquals(200, d.width());
        assertEquals(100, d.height());
        assertEquals(-40, d.x()); // 10 + (100-200)/2
        assertEquals(-5, d.y());  // 20 + (50-100)/2
    }

    @Test
    void drawAppliesScalePercentHalf() {
        // 区域 100x50，图片 200x100 → FIT 100x50；scalePercent=0.5 → 50x25，区域居中
        SlotRenderState s = new SlotRenderState(Slot.V, key, FrameSequence.single("x", f1),
                new Region(10, 20, 100, 50), MoveMode.fixed(), ScaleMode.FIT, 0.5, 0, 0, new Random(1));
        s.advance(100);
        SlotRenderState.Draw d = s.draw(200, 100);
        assertEquals(50, d.width());
        assertEquals(25, d.height());
        assertEquals(35, d.x()); // 10 + (100-50)/2
        assertEquals(32, d.y()); // 20 + (50-25)/2
    }

    @Test
    void drawStretchFillsRegion() {
        SlotRenderState s = state(new Region(0, 0, 90, 90), MoveMode.fixed(), ScaleMode.STRETCH,
                FrameSequence.single("x", f1), 0, 0);
        s.advance(100);
        SlotRenderState.Draw d = s.draw(200, 100);
        assertEquals(90, d.width());
        assertEquals(90, d.height());
        assertEquals(0, d.x());
        assertEquals(0, d.y());
    }

    @Test
    void drawAppliesDirectionOffset() {
        SlotRenderState s = state(new Region(10, 20, 90, 90), MoveMode.direction(0, -8), ScaleMode.STRETCH,
                FrameSequence.single("x", f1), 0, 0);
        s.advance(100);
        SlotRenderState.Draw d = s.draw(90, 90);
        assertEquals(10, d.x());
        assertEquals(20 - 8, d.y());
    }

    @Test
    void drawAppliesRandomOffsetWithinDistance() {
        SlotRenderState s = state(new Region(10, 20, 90, 90), MoveMode.random(4, 500), ScaleMode.STRETCH,
                FrameSequence.single("x", f1), 0, 0);
        s.advance(100);
        for (int i = 0; i < 50; i++) {
            SlotRenderState.Draw d = s.draw(90, 90);
            assertTrue(Math.abs(d.x() - 10) <= 4, "dx out of range: " + d.x());
            assertTrue(Math.abs(d.y() - 20) <= 4, "dy out of range: " + d.y());
        }
    }

    @Test
    void invisibleWhenAlphaZero() {
        SlotRenderState s = state(new Region(0, 0, 90, 90), MoveMode.fixed(), ScaleMode.STRETCH,
                FrameSequence.single("x", f1), 300, 0);
        SlotRenderState.Draw d = s.draw(90, 90); // 未推进，alpha=0
        assertFalse(d.visible());
    }

    @Test
    void regionOverlap() {
        Region a = new Region(0, 0, 10, 10);
        assertTrue(a.overlaps(new Region(5, 5, 10, 10)));
        assertFalse(a.overlaps(new Region(10, 0, 10, 10)));
        assertFalse(a.overlaps(new Region(20, 20, 5, 5)));
        assertThrows(IllegalArgumentException.class, () -> new Region(0, 0, -1, 5));
    }
}
