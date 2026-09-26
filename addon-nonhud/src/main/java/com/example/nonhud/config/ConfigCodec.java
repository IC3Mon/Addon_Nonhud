package com.example.nonhud.config;

import com.example.nonhud.render.MoveMode;
import com.example.nonhud.render.Region;
import com.example.nonhud.render.ScaleMode;
import com.example.nonhud.scan.Slot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * config.json 编解码（设计文档 §6 草案结构），容错缺省：损坏/缺字段 → 默认值。
 *
 * JSON 结构：
 * {
 *   "version": 1,
 *   "fps": 2.0,
 *   "slotsEnabled": {"A": true, ...},        // 缺省全开
 *   "keys": {"pause": "key.keyboard.p", "settings": "key.keyboard.f6"},
 *   "zOrder": ["fxxkme", ...],
 *   "effect": {"fadeInMs": 300, "fadeOutMs": 300, "scaleMode": "fit"},
 *   "regions": {"A": {"x":15,"y":40,"width":90,"height":90}, ...},
 *   "moveModes": {"V": {"type":"direction","dx":0,"dy":-8}, ...},
 *   "thresholds": {"energy":[50,100,150],"vTank":[30,60,100],"aTank":[30,60,100],"pregnancy":[85,170,255]}
 * }
 */
public final class ConfigCodec {

    private static final Gson GSON = new Gson();

    private ConfigCodec() { }

    /** Config → JSON 字符串（pretty）。 */
    public static String toJson(Config cfg) {
        JsonObject root = new JsonObject();
        root.addProperty("version", cfg.VERSION);
        root.addProperty("fps", cfg.fps);

        JsonObject enabled = new JsonObject();
        for (Slot s : Slot.values()) {
            enabled.addProperty(s.name().toLowerCase(Locale.ROOT), cfg.slotsEnabled.contains(s));
        }
        root.add("slotsEnabled", enabled);

        JsonObject keys = new JsonObject();
        keys.addProperty("pause", cfg.pauseKey);
        keys.addProperty("settings", cfg.settingsKey);
        root.add("keys", keys);

        JsonArray z = new JsonArray();
        for (Slot s : cfg.zOrder) {
            z.add(s.name().toLowerCase(Locale.ROOT));
        }
        root.add("zOrder", z);

        JsonObject effect = new JsonObject();
        effect.addProperty("fadeInMs", cfg.fadeInMs);
        effect.addProperty("fadeOutMs", cfg.fadeOutMs);
        effect.addProperty("scaleMode", cfg.scaleMode.name().toLowerCase(Locale.ROOT));
        effect.addProperty("scalePercent", cfg.scalePercent);
        root.add("effect", effect);

        root.addProperty("numericDurationMs", cfg.numericDurationMs);

        JsonObject regions = new JsonObject();
        for (Map.Entry<Slot, Region> e : cfg.regions.entrySet()) {
            Region r = e.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("x", r.x());
            o.addProperty("y", r.y());
            o.addProperty("width", r.width());
            o.addProperty("height", r.height());
            regions.add(e.getKey().name().toLowerCase(Locale.ROOT), o);
        }
        root.add("regions", regions);

        JsonObject moves = new JsonObject();
        for (Map.Entry<Slot, MoveMode> e : cfg.moveModes.entrySet()) {
            MoveMode m = e.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("type", m.type().name().toLowerCase(Locale.ROOT));
            o.addProperty("dx", m.dx());
            o.addProperty("dy", m.dy());
            o.addProperty("distance", m.distance());
            o.addProperty("refreshMs", m.refreshMs());
            moves.add(e.getKey().name().toLowerCase(Locale.ROOT), o);
        }
        root.add("moveModes", moves);

        JsonObject thresholds = new JsonObject();
        thresholds.add("energy", ints(cfg.energyThresholds));
        thresholds.add("vTank", ints(cfg.vTankThresholds));
        thresholds.add("aTank", ints(cfg.aTankThresholds));
        thresholds.add("pregnancy", ints(cfg.pregnancyThresholds));
        root.add("thresholds", thresholds);

        return GSON.toJson(root);
    }

    private static JsonArray ints(List<Integer> list) {
        JsonArray a = new JsonArray();
        for (int i : list) {
            a.add(i);
        }
        return a;
    }

    /**
     * JSON 字符串 → Config；损坏 JSON 抛 {@link JsonParseException}（调用方静默兜底为默认）。
     * 缺字段/非法枚举值 → 默认值容错。
     */
    public static Config fromJson(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Config cfg = new Config();
        cfg.fps = optDouble(root, "fps", cfg.fps);

        JsonObject enabled = optObj(root, "slotsEnabled");
        for (Slot s : Slot.values()) {
            JsonElement e = enabled.get(s.name().toLowerCase(Locale.ROOT));
            if (e != null && !e.getAsBoolean()) {
                cfg.slotsEnabled.remove(s);
            }
        }

        JsonObject keys = optObj(root, "keys");
        cfg.pauseKey = optStr(keys, "pause", cfg.pauseKey);
        cfg.settingsKey = optStr(keys, "settings", cfg.settingsKey);

        JsonArray z = root.getAsJsonArray("zOrder");
        if (z != null) {
            List<Slot> order = new ArrayList<>();
            for (JsonElement e : z) {
                Slot s = parseSlot(e.getAsString());
                if (s != null && !order.contains(s)) {
                    order.add(s);
                }
            }
            cfg.zOrder = order;
        }

        JsonObject effect = optObj(root, "effect");
        cfg.fadeInMs = optLong(effect, "fadeInMs", cfg.fadeInMs);
        cfg.fadeOutMs = optLong(effect, "fadeOutMs", cfg.fadeOutMs);
        cfg.scaleMode = parseScaleMode(optStr(effect, "scaleMode", cfg.scaleMode.name()));
        cfg.scalePercent = optDouble(effect, "scalePercent", cfg.scalePercent);
        cfg.numericDurationMs = optLong(root, "numericDurationMs", cfg.numericDurationMs);

        JsonObject regions = optObj(root, "regions");
        for (Map.Entry<String, JsonElement> e : regions.entrySet()) {
            Slot s = parseSlot(e.getKey());
            JsonObject o = e.getValue().getAsJsonObject();
            if (s != null) {
                cfg.regions.put(s, new Region(
                        optInt(o, "x", 0), optInt(o, "y", 0),
                        optInt(o, "width", 90), optInt(o, "height", 90)));
            }
        }

        JsonObject moves = optObj(root, "moveModes");
        for (Map.Entry<String, JsonElement> e : moves.entrySet()) {
            Slot s = parseSlot(e.getKey());
            JsonObject o = e.getValue().getAsJsonObject();
            if (s == null) {
                continue;
            }
            String type = optStr(o, "type", "fixed").toLowerCase(Locale.ROOT);
            MoveMode mode = switch (type) {
                case "direction" -> MoveMode.direction(optInt(o, "dx", 0), optInt(o, "dy", 0));
                case "random" -> MoveMode.random(optInt(o, "distance", 4), optLong(o, "refreshMs", 500));
                default -> MoveMode.fixed();
            };
            cfg.moveModes.put(s, mode);
        }

        JsonObject thresholds = optObj(root, "thresholds");
        cfg.energyThresholds = optInts(thresholds.get("energy"), cfg.energyThresholds);
        cfg.vTankThresholds = optInts(thresholds.get("vTank"), cfg.vTankThresholds);
        cfg.aTankThresholds = optInts(thresholds.get("aTank"), cfg.aTankThresholds);
        cfg.pregnancyThresholds = optInts(thresholds.get("pregnancy"), cfg.pregnancyThresholds);

        return cfg;
    }

    private static List<Integer> optInts(JsonElement e, List<Integer> fallback) {
        if (e == null || !e.isJsonArray()) {
            return fallback;
        }
        List<Integer> out = new ArrayList<>();
        for (JsonElement x : e.getAsJsonArray()) {
            if (x.isJsonPrimitive() && x.getAsJsonPrimitive().isNumber()) {
                out.add(x.getAsInt());
            }
        }
        return out.isEmpty() ? fallback : out;
    }

    private static Slot parseSlot(String name) {
        try {
            return Slot.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static ScaleMode parseScaleMode(String name) {
        try {
            return ScaleMode.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            return ScaleMode.FIT;
        }
    }

    private static JsonObject optObj(JsonObject parent, String key) {
        JsonElement e = parent.get(key);
        return (e != null && e.isJsonObject()) ? e.getAsJsonObject() : new JsonObject();
    }

    private static String optStr(JsonObject o, String key, String fallback) {
        JsonElement e = o.get(key);
        return (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isString())
                ? e.getAsString() : fallback;
    }

    private static double optDouble(JsonObject o, String key, double fallback) {
        JsonElement e = o.get(key);
        return (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())
                ? e.getAsDouble() : fallback;
    }

    private static long optLong(JsonObject o, String key, long fallback) {
        JsonElement e = o.get(key);
        return (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())
                ? e.getAsLong() : fallback;
    }

    private static int optInt(JsonObject o, String key, int fallback) {
        JsonElement e = o.get(key);
        return (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())
                ? e.getAsInt() : fallback;
    }
}
