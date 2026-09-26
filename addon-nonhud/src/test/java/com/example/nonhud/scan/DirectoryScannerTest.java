package com.example.nonhud.scan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** 目录扫描：两级目录识别、两类文件名解析、兜底单图、非图忽略、缺目录容错、O4 槽开关。 */
class DirectoryScannerTest {

    @TempDir
    Path temp;

    @BeforeEach
    void setUpTree() throws IOException {
        file("event/V/pre", "A1-f01.png");
        file("event/V/pre", "A1-f02.png");
        file("event/V/pre", "A1-f03.png");
        file("event/V/pre", "myimg.jpg");          // 兜底单图
        file("event/V/pre", "notes.txt");          // 非图片，忽略
        file("event/V/pre", "anim.gif");           // 非 jpg/png，忽略
        file("event/V/peak", "B1-f02.png");        // 乱序创建，按帧号排序
        file("event/V/peak", "B1-f01.png");
        // event/V/post 留空
        file("event/A/female/pre", "FA-f01.png");
        file("event/A/male/pre", "MA-f01.png");
        file("event/A/male/pre", "MA-f02.png");
        file("event/botle", "botle_f01.jpg");
        file("numeric/Vfull/up", "Vfullup1-ph1-f01.png");
        file("numeric/Vfull/up", "Vfullup1-ph1-f02.png");
        file("numeric/Vfull/up", "Vfullup2-ph1-f01.png");
        file("numeric/Vfull/re", "Vfullre1-ph2-f01.png");
        file("numeric/Afull/up", "Afullup1-ph1-f01.png");
        file("numeric/pregnant", "pg-ph1-f01.png");
        file("numeric/pregnant", "pg-ph2-f01.png");
        file("numeric/pregnant/birth", "birth_f01.png");
        file("numeric/fxxkme", "fx-ph1-f01.png");
        file("numeric/fxxkme", "fx-ph1-f02.png");
        file("numeric/fxxkme", "fx-ph2-f01.png");
    }

    @Test
    void eventSlotPrefixedFramesAndSingleFallback() {
        SlotIndex idx = DirectoryScanner.scan(temp, null);

        List<FrameSequence> pre = idx.get(SlotKey.event(Slot.V, EventTier.PRE));
        Set<String> prefixes = pre.stream().map(FrameSequence::prefix).collect(Collectors.toSet());
        assertEquals(Set.of("A1", "myimg"), prefixes, "同阶段多前缀构成候选，兜底单图一并装载");

        FrameSequence a1 = pre.stream().filter(f -> f.prefix().equals("A1")).findFirst().orElseThrow();
        assertEquals(3, a1.size());
        assertFalse(a1.single());
        assertEquals(Path.of(temp.resolve("event/V/pre").resolve("A1-f01.png").toString()), a1.frames().get(0));
        assertEquals(Path.of(temp.resolve("event/V/pre").resolve("A1-f03.png").toString()), a1.frames().get(2));

        FrameSequence myimg = pre.stream().filter(f -> f.prefix().equals("myimg")).findFirst().orElseThrow();
        assertTrue(myimg.single());
        assertEquals(1, myimg.size());
    }

    @Test
    void framesAreSortedByFrameNumberNotNameOrder() {
        SlotIndex idx = DirectoryScanner.scan(temp, null);
        List<FrameSequence> peak = idx.get(SlotKey.event(Slot.V, EventTier.PEAK));
        assertEquals(1, peak.size());
        FrameSequence b1 = peak.get(0);
        assertEquals("B1", b1.prefix());
        assertEquals(2, b1.size());
        assertTrue(b1.frames().get(0).getFileName().toString().endsWith("f01.png"));
        assertTrue(b1.frames().get(1).getFileName().toString().endsWith("f02.png"));
    }

    @Test
    void emptyOrMissingStageDirYieldsNoKey() {
        SlotIndex idx = DirectoryScanner.scan(temp, null);
        // event/V/post 为空目录 → 无键、不报错
        assertTrue(idx.get(SlotKey.event(Slot.V, EventTier.POST)).isEmpty());
        // 整个根目录不存在 → 空索引
        SlotIndex missing = DirectoryScanner.scan(temp.resolve("nope"), null);
        assertTrue(missing.slots().isEmpty());
        assertEquals(0, missing.size());
    }

    @Test
    void genderLevelsForASlot() {
        SlotIndex idx = DirectoryScanner.scan(temp, null);
        List<FrameSequence> female = idx.get(SlotKey.eventA(Gender.FEMALE, EventTier.PRE));
        List<FrameSequence> male = idx.get(SlotKey.eventA(Gender.MALE, EventTier.PRE));
        assertEquals(List.of("FA"), female.stream().map(FrameSequence::prefix).toList());
        assertEquals(List.of("MA"), male.stream().map(FrameSequence::prefix).toList());
        assertEquals(2, male.get(0).size());
    }

    @Test
    void botleIsSingleTier() {
        SlotIndex idx = DirectoryScanner.scan(temp, null);
        List<FrameSequence> botle = idx.get(SlotKey.botle());
        assertEquals(1, botle.size());
        assertEquals("botle", botle.get(0).prefix());
    }

    @Test
    void numericSlotPhTiersAndDirections() {
        SlotIndex idx = DirectoryScanner.scan(temp, null);

        List<FrameSequence> vUpPh1 = idx.get(SlotKey.numeric(Slot.VFULL, Direction.UP, 1));
        assertEquals(Set.of("Vfullup1", "Vfullup2"),
                vUpPh1.stream().map(FrameSequence::prefix).collect(Collectors.toSet()));
        assertEquals(2, vUpPh1.stream().filter(f -> f.prefix().equals("Vfullup1")).findFirst().orElseThrow().size());

        List<FrameSequence> vRePh2 = idx.get(SlotKey.numeric(Slot.VFULL, Direction.RE, 2));
        assertEquals(List.of("Vfullre1"), vRePh2.stream().map(FrameSequence::prefix).toList());

        List<FrameSequence> aUpPh1 = idx.get(SlotKey.numeric(Slot.AFULL, Direction.UP, 1));
        assertEquals(List.of("Afullup1"), aUpPh1.stream().map(FrameSequence::prefix).toList());

        // ph 档位互不串扰：UP/ph2 与 RE/ph1 均无数据
        assertTrue(idx.get(SlotKey.numeric(Slot.VFULL, Direction.UP, 2)).isEmpty());
        assertTrue(idx.get(SlotKey.numeric(Slot.VFULL, Direction.RE, 1)).isEmpty());
    }

    @Test
    void fxxkmeAndPregnantParsing() {
        SlotIndex idx = DirectoryScanner.scan(temp, null);

        List<FrameSequence> fxPh1 = idx.get(SlotKey.fxxkme(1));
        assertEquals(List.of("fx"), fxPh1.stream().map(FrameSequence::prefix).toList());
        assertEquals(2, fxPh1.get(0).size());
        List<FrameSequence> fxPh2 = idx.get(SlotKey.fxxkme(2));
        assertEquals(1, fxPh2.get(0).size());

        assertEquals(List.of("pg"), idx.get(SlotKey.pregnant(1)).stream().map(FrameSequence::prefix).toList());
        assertEquals(List.of("pg"), idx.get(SlotKey.pregnant(2)).stream().map(FrameSequence::prefix).toList());
        assertTrue(idx.get(SlotKey.pregnant(3)).isEmpty());

        List<FrameSequence> birth = idx.get(SlotKey.birth());
        assertEquals(1, birth.size());
        assertEquals("birth", birth.get(0).prefix());
        assertFalse(birth.get(0).single(), "birth 带 _fYY 应为帧序列而非单图");
    }

    @Test
    void nonImageFilesIgnored() {
        SlotIndex idx = DirectoryScanner.scan(temp, null);
        // notes.txt / anim.gif 不产生任何候选
        boolean leaked = idx.slots().stream().anyMatch(s -> s != null);
        assertTrue(leaked); // 索引非空
        for (List<FrameSequence> candidates : List.of(
                idx.get(SlotKey.event(Slot.V, EventTier.PRE)),
                idx.get(SlotKey.event(Slot.V, EventTier.PEAK)))) {
            assertTrue(candidates.stream().noneMatch(f -> f.prefix().contains("notes") || f.prefix().contains("anim")),
                    "非图片文件不得进入索引");
        }
    }

    @Test
    void disabledSlotIsNotScanned() {
        SlotIndex idx = DirectoryScanner.scan(temp,
                EnumSet.of(Slot.V, Slot.A, Slot.VFULL, Slot.AFULL, Slot.FXXKME));
        assertFalse(idx.has(Slot.BOTLE), "O4：禁用槽不扫描");
        assertFalse(idx.has(Slot.PREGNANT), "O4：禁用槽不扫描");
        assertTrue(idx.has(Slot.V));
    }

    @Test
    void bothSeparatorsAccepted() throws IOException {
        // §2.2 规范写法（下划线 前缀_fYY）与 §11.2 示例写法（连字符 前缀-fYY）都装载
        file("event/M/peak", "M1_f01.png");
        file("event/M/peak", "M2-f01.png");
        SlotIndex idx = DirectoryScanner.scan(temp, null);
        List<FrameSequence> peak = idx.get(SlotKey.event(Slot.M, EventTier.PEAK));
        assertEquals(Set.of("M1", "M2"),
                peak.stream().map(FrameSequence::prefix).collect(Collectors.toSet()));
    }

    /** 在相对目录下创建空占位文件（解析只看文件名）。 */
    private void file(String relDir, String name) throws IOException {
        Path dir = temp.resolve(relDir);
        Files.createDirectories(dir);
        Files.write(dir.resolve(name), new byte[0]);
    }
}
