package com.example.nonhud;

import com.example.nonhud.scan.Direction;
import com.example.nonhud.scan.DirectoryScanner;
import com.example.nonhud.scan.EventTier;
import com.example.nonhud.scan.Gender;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotIndex;
import com.example.nonhud.scan.SlotKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 步骤 7 首次解压核心逻辑（FirstRunExtractor.copyMissingRecursively）：
 * 仅补缺、不覆盖——空目标全量复制 / 已存在文件保留 / 缺失文件补全 / 源非目录静默。
 */
class FirstRunExtractorTest {

    @TempDir
    Path temp;

    @Test
    void emptyTargetCopiesWholeTree() throws IOException {
        Path src = temp.resolve("src");
        Files.createDirectories(src.resolve("sub"));
        Files.writeString(src.resolve("a.png"), "A");
        Files.writeString(src.resolve("sub").resolve("b.png"), "B");

        Path target = temp.resolve("dst");
        FirstRunExtractor.copyMissingRecursively(src, target);

        assertEquals("A", Files.readString(target.resolve("a.png")));
        assertEquals("B", Files.readString(target.resolve("sub").resolve("b.png")));
    }

    @Test
    void existingFileIsNotOverwritten() throws IOException {
        Path src = temp.resolve("src");
        Files.createDirectories(src);
        Files.writeString(src.resolve("a.png"), "new-content");

        Path target = temp.resolve("dst");
        Files.createDirectories(target);
        Files.writeString(target.resolve("a.png"), "player-content");

        FirstRunExtractor.copyMissingRecursively(src, target);

        // 玩家已有文件保留（仅补缺不覆盖）
        assertEquals("player-content", Files.readString(target.resolve("a.png")));
    }

    @Test
    void missingFilesAreFilledAlongsideExisting() throws IOException {
        Path src = temp.resolve("src");
        Files.createDirectories(src.resolve("sub"));
        Files.writeString(src.resolve("keep.png"), "new-keep");
        Files.writeString(src.resolve("sub").resolve("fill.png"), "new-fill");

        Path target = temp.resolve("dst");
        Files.createDirectories(target.resolve("sub"));
        Files.writeString(target.resolve("keep.png"), "player-keep");

        FirstRunExtractor.copyMissingRecursively(src, target);

        assertEquals("player-keep", Files.readString(target.resolve("keep.png"))); // 未覆盖
        assertEquals("new-fill", Files.readString(target.resolve("sub").resolve("fill.png"))); // 补缺
    }

    @Test
    void nonDirectorySourceIsNoOp() throws IOException {
        Path src = temp.resolve("nonexistent");
        Path target = temp.resolve("dst");

        FirstRunExtractor.copyMissingRecursively(src, target);

        assertFalse(Files.exists(target));
    }

    @Test
    void builtInPackExtractsAndIndexesAllSlots() throws IOException {
        // 全链路（打包→解压→索引）：定位打包进 classpath 的默认 NoNHud 资源树，
        // 仅补缺解压到临时目录后，用 DirectoryScanner 重建索引（O1），断言 8 槽全档位就位。
        URL url = FirstRunExtractor.class.getClassLoader().getResource(FirstRunExtractor.DEFAULT_ROOT);
        assertNotNull(url, "built-in NoNHud pack must be packaged into resources");
        Path src;
        try {
            src = Path.of(url.toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IOException("bad resource URI: " + url, e);
        }
        Path target = temp.resolve("extracted");
        FirstRunExtractor.copyMissingRecursively(src, target);

        SlotIndex index = DirectoryScanner.scan(target, null);

        // 全部 8 个槽均被索引（O4 全启用）
        assertEquals(8, index.slots().size(), "all 8 slots indexed");
        // event 槽：A female/male × pre/peak/post、V/M × pre/peak/post、botle 单档
        for (Gender g : Gender.values()) {
            for (EventTier t : new EventTier[]{EventTier.PRE, EventTier.PEAK, EventTier.POST}) {
                assertFalse(index.get(SlotKey.eventA(g, t)).isEmpty(), "A/" + g + "/" + t);
            }
        }
        for (EventTier t : new EventTier[]{EventTier.PRE, EventTier.PEAK, EventTier.POST}) {
            assertFalse(index.get(SlotKey.event(Slot.V, t)).isEmpty(), "V/" + t);
            assertFalse(index.get(SlotKey.event(Slot.M, t)).isEmpty(), "M/" + t);
        }
        assertFalse(index.get(SlotKey.botle()).isEmpty(), "botle");
        // numeric 槽：Vfull/Afull × up/re × ph1/ph2、pregnant ph1/ph2 + birth、fxxkme ph1/ph2
        for (Direction d : Direction.values()) {
            for (int ph = 1; ph <= 2; ph++) {
                assertFalse(index.get(SlotKey.numeric(Slot.VFULL, d, ph)).isEmpty(), "Vfull/" + d + "/ph" + ph);
                assertFalse(index.get(SlotKey.numeric(Slot.AFULL, d, ph)).isEmpty(), "Afull/" + d + "/ph" + ph);
            }
        }
        for (int ph = 1; ph <= 2; ph++) {
            assertFalse(index.get(SlotKey.pregnant(ph)).isEmpty(), "pregnant/ph" + ph);
        }
        assertFalse(index.get(SlotKey.birth()).isEmpty(), "pregnant/birth");
        for (int ph = 1; ph <= 2; ph++) {
            assertFalse(index.get(SlotKey.fxxkme(ph)).isEmpty(), "fxxkme/ph" + ph);
        }
    }
}
