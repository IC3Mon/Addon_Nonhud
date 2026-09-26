package com.example.nonhud.config;

import com.example.nonhud.render.ScaleMode;
import com.example.nonhud.scan.Slot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** 配置管理器：缺省落盘 / 读取 / 损坏容错 / 更新并应用（O3/O4）。 */
class ConfigManagerTest {

    @TempDir
    Path tmp;

    @Test
    void missingFileWritesDefaults() throws IOException {
        ConfigManager mgr = new ConfigManager(tmp);
        mgr.load(true);
        Path file = tmp.resolve(ConfigManager.FILE_NAME);
        assertTrue(Files.isRegularFile(file));
        String json = Files.readString(file);
        assertTrue(json.contains("\"fps\""));
        assertTrue(json.contains("\"slotsEnabled\""));
        assertTrue(json.contains("\"zOrder\""));
        assertEquals(2.0, mgr.current().fps, 1e-6);
        assertTrue(mgr.current().slotEnabled(Slot.A));
    }

    @Test
    void loadReadsSavedValues() throws IOException {
        ConfigManager mgr = new ConfigManager(tmp);
        mgr.load(true);
        Config c = mgr.current();
        c.fps = 7.0;
        c.scaleMode = ScaleMode.STRETCH;
        c.slotsEnabled.remove(Slot.M);
        mgr.updateAndApply(c);

        ConfigManager mgr2 = new ConfigManager(tmp);
        mgr2.load(true);
        assertEquals(7.0, mgr2.current().fps, 1e-6);
        assertEquals(ScaleMode.STRETCH, mgr2.current().scaleMode);
        assertFalse(mgr2.current().slotEnabled(Slot.M));
        assertTrue(mgr2.current().slotEnabled(Slot.V));
    }

    @Test
    void corruptedFileFallsBackWithoutOverwriting() throws IOException {
        Path file = tmp.resolve(ConfigManager.FILE_NAME);
        Files.writeString(file, "{broken json");
        ConfigManager mgr = new ConfigManager(tmp);
        mgr.load(false);
        assertEquals(2.0, mgr.current().fps, 1e-6); // 默认
        assertEquals("{broken json", Files.readString(file)); // 原文件未动

        // 显式 overwrite=true 时才覆盖为有效默认
        ConfigManager mgr2 = new ConfigManager(tmp);
        mgr2.load(true);
        assertTrue(Files.readString(file).contains("\"fps\""));
    }

    @Test
    void emptyEnabledSetMeansAllEnabled() {
        Config c = new Config();
        assertTrue(c.slotEnabled(Slot.A));
        assertTrue(c.slotEnabled(Slot.FXXKME));
    }
}
