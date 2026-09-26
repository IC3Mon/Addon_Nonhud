package com.example.nonhud.scan;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 帧序列：循环推进 / 单图固定 / 重置 / 不可变帧列表。 */
class FrameSequenceTest {

    private final Path f1 = Path.of("f01.png");
    private final Path f2 = Path.of("f02.png");
    private final Path f3 = Path.of("f03.png");

    @Test
    void sequenceAdvancesAndLoops() {
        FrameSequence seq = FrameSequence.sequence("A1", List.of(f1, f2, f3), 2.0);
        assertEquals(f1, seq.current());

        // fps=2 每秒 2 帧：10 游戏 tick = 0.5 秒 → 1 帧
        seq.advanceTicks(10);
        assertEquals(f2, seq.current());

        seq.advanceTicks(10); // 再 1 帧 → f3
        assertEquals(f3, seq.current());

        seq.advanceTicks(20); // 再 2 帧 → (2+2) % 3 = 1
        assertEquals(f2, seq.current());

        seq.advanceTicks(10); // 再 1 帧 → (1+1) % 3 = 2
        assertEquals(f3, seq.current());

        seq.advanceTicks(1);  // 0.05 秒不足一帧（carry 0.1），不推进
        assertEquals(f3, seq.current());

        seq.advanceTicks(9);  // 累积 0.1 + 0.9 = 1.0 → 1 帧
        assertEquals(f1, seq.current());
    }

    @Test
    void singleImageNeverAdvances() {
        FrameSequence single = FrameSequence.single("myimg", f1);
        assertEquals(1, single.size());
        assertTrue(single.single());

        single.advanceTicks(100);
        assertEquals(f1, single.current());
    }

    @Test
    void singleFrameSequenceDoesNotAdvance() {
        FrameSequence one = FrameSequence.sequence("one", List.of(f1), 2.0);
        one.advanceTicks(10);
        assertEquals(f1, one.current());
    }

    @Test
    void resetReturnsToFirstFrame() {
        FrameSequence seq = FrameSequence.sequence("A1", List.of(f1, f2, f3), 2.0);
        seq.advanceTicks(10);
        seq.reset();
        assertEquals(f1, seq.current());
    }

    @Test
    void framesListIsImmutable() {
        FrameSequence seq = FrameSequence.sequence("A1", List.of(f1, f2, f3), 2.0);
        assertThrows(UnsupportedOperationException.class, () -> seq.frames().add(f1));
    }

    @Test
    void emptyFramesRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> FrameSequence.sequence("x", List.of(), 2.0));
    }
}
