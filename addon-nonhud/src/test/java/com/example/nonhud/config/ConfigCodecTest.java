package com.example.nonhud.config;

import com.example.nonhud.render.Group;
import com.example.nonhud.render.MoveMode;
import com.example.nonhud.render.Region;
import com.example.nonhud.render.ScaleMode;
import com.example.nonhud.scan.Slot;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** config.json 编解码：往返一致 / 缺省容错 / 损坏容错。 */
class ConfigCodecTest {

    @Test
    void roundTripPreservesAllFields() {
        Config c = new Config();
        c.fps = 3.5;
        c.pauseKey = "key.keyboard.p";
        c.settingsKey = "key.keyboard.f6";
        c.zOrder = List.of(Slot.FXXKME, Slot.PREGNANT, Slot.A);
        c.fadeInMs = 150;
        c.fadeOutMs = 400;
        c.scaleMode = ScaleMode.STRETCH;
        c.scalePercent = 2.5;
        c.numericDurationMs = 4500;
        c.regions.put(Slot.V, new Region(10, 20, 120, 80));
        c.moveModes.put(Slot.M, MoveMode.random(6, 700));
        c.energyThresholds = List.of(60, 120);

        String json = ConfigCodec.toJson(c);
        Config back = ConfigCodec.fromJson(json);

        assertEquals(3.5, back.fps, 1e-6);
        assertEquals("key.keyboard.p", back.pauseKey);
        assertEquals("key.keyboard.f6", back.settingsKey);
        assertEquals(List.of(Slot.FXXKME, Slot.PREGNANT, Slot.A), back.zOrder);
        assertEquals(150, back.fadeInMs);
        assertEquals(400, back.fadeOutMs);
        assertEquals(ScaleMode.STRETCH, back.scaleMode);
        assertEquals(2.5, back.scalePercent, 1e-6);
        assertEquals(4500, back.numericDurationMs);
        assertEquals(new Region(10, 20, 120, 80), back.regions.get(Slot.V));
        MoveMode m = back.moveModes.get(Slot.M);
        assertEquals(MoveMode.Type.RANDOM, m.type());
        assertEquals(6, m.distance());
        assertEquals(700, m.refreshMs());
        assertEquals(List.of(60, 120), back.energyThresholds);
    }

    @Test
    void slotToggleRoundTrip() {
        Config c = new Config();
        c.slotsEnabled.remove(Slot.M);
        c.slotsEnabled.remove(Slot.VFULL);
        Config back = ConfigCodec.fromJson(ConfigCodec.toJson(c));
        assertFalse(back.slotEnabled(Slot.M));
        assertFalse(back.slotEnabled(Slot.VFULL));
        assertTrue(back.slotEnabled(Slot.A));
        assertTrue(back.slotEnabled(Slot.FXXKME));
    }

    @Test
    void emptyJsonFallsBackToDefaults() {
        Config back = ConfigCodec.fromJson("{}");
        assertEquals(Config.VERSION, 1);
        assertEquals(2.0, back.fps, 1e-6);
        assertTrue(back.slotEnabled(Slot.A));
        assertEquals(ScaleMode.FIT, back.scaleMode);
        assertEquals(300, back.fadeInMs);
        assertEquals(1.0, back.scalePercent, 1e-6);
        assertEquals(3000, back.numericDurationMs);
        assertEquals(8, back.zOrder.size());
        assertEquals(8, back.regions.size());
        assertEquals(8, back.moveModes.size());
        assertEquals(List.of(50, 100, 150), back.energyThresholds);
    }

    @Test
    void partialJsonFillsMissingWithDefaults() {
        String json = "{\"fps\":5,\"effect\":{\"scaleMode\":\"stretch\"}}";
        Config back = ConfigCodec.fromJson(json);
        assertEquals(5.0, back.fps, 1e-6);
        assertEquals(ScaleMode.STRETCH, back.scaleMode);
        assertEquals(300, back.fadeOutMs); // 缺省
        assertEquals(8, back.regions.size());
    }

    @Test
    void corruptedJsonThrows() {
        assertThrows(JsonParseException.class, () -> ConfigCodec.fromJson("{not json"));
    }

    @Test
    void invalidEnumAndNumbersAreTolerated() {
        String json = "{\"fps\":\"abc\",\"zOrder\":[\"notaslot\",\"A\",\"A\"],\"effect\":{\"scaleMode\":\"bogus\"}}";
        Config back = ConfigCodec.fromJson(json);
        assertEquals(2.0, back.fps, 1e-6); // 非法数字 → 默认
        assertEquals(ScaleMode.FIT, back.scaleMode); // 非法枚举 → 默认
        assertEquals(List.of(Slot.A), back.zOrder); // 未知槽被忽略、重复去重
    }

    @Test
    void groupsFxxkmeAndKeysRoundTrip() {
        Config c = new Config();
        c.groups = new ArrayList<>(List.of(
                new Group("g1", List.of(Slot.A, Slot.V, Slot.M),
                        new Region(20, 30, 610, 160), 3.0, 100, 200,
                        true, 30, Group.Layer.FRONT)));
        c.fxxkmeEnergyThreshold = 250;
        c.fxxkmeIntervalMinSec = 40;
        c.fxxkmeIntervalMaxSec = 90;
        c.fillBottleAnimId = "my_fill_bottle";

        Config back = ConfigCodec.fromJson(ConfigCodec.toJson(c));
        assertEquals(1, back.groups.size());
        Group g = back.groups.get(0);
        assertEquals("g1", g.id());
        assertEquals(List.of(Slot.A, Slot.V, Slot.M), g.slots());
        assertEquals(new Region(20, 30, 610, 160), g.area());
        assertEquals(3.0, g.fps(), 1e-6);
        assertEquals(100, g.fadeInMs());
        assertEquals(200, g.fadeOutMs());
        assertTrue(g.overlapEnabled());
        assertEquals(30, g.overlapPercent());
        assertEquals(Group.Layer.FRONT, g.layer());
        assertEquals(250, back.fxxkmeEnergyThreshold);
        assertEquals(40, back.fxxkmeIntervalMinSec);
        assertEquals(90, back.fxxkmeIntervalMaxSec);
        assertEquals("my_fill_bottle", back.fillBottleAnimId);
    }

    @Test
    void emptyJsonHasDefaultGroups() {
        Config back = ConfigCodec.fromJson("{}");
        assertEquals(3, back.groups.size());
        assertEquals("g1", back.groups.get(0).id());
    }

    @Test
    void invalidGroupEntriesAreDropped() {
        String json = "{\"groups\":[{\"id\":\"\",\"slots\":[\"A\"]},{\"id\":\"g1\",\"slots\":[],\"area\":{}}]}";
        Config back = ConfigCodec.fromJson(json);
        // 空 id / 空 slots 的组被丢弃 → 保留默认组
        assertEquals(3, back.groups.size());
    }
}
