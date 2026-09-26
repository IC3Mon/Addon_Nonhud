package com.example.nonhud.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 从 animdef JSON 中提取 actor 的 injector 角色（设计文档 §3.4，README 1382/1401-1412）。
 *
 * NoN 公共 API 的 NonAnimationActor 不暴露 injector 字段（javap 核对结论），
 * 因此从资源包的 data/&lt;ns&gt;/afw_animdefs/&lt;动画ID&gt;.json 解析：
 * - "injector": true → 泛型 V；
 * - "injector": "V" | "A" | "M"（大小写不敏感）→ 对应角色；
 * - "injector": false / 缺失 → 无注入角色。
 */
public final class AnimdefParser {

    private AnimdefParser() { }

    /** 解析 actors[].injector 角色集；缺字段容错为空；JSON 损坏抛 JsonParseException。 */
    public static Set<String> injectorRoles(String json) {
        Set<String> roles = new LinkedHashSet<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (!root.has("actors")) {
            return roles;
        }
        JsonArray actors = root.getAsJsonArray("actors");
        for (JsonElement element : actors) {
            JsonObject actor = element.getAsJsonObject();
            JsonElement injector = actor.get("injector");
            if (injector == null || injector.isJsonNull()) {
                continue;
            }
            if (injector.isJsonPrimitive() && injector.getAsJsonPrimitive().isBoolean()) {
                if (injector.getAsBoolean()) {
                    roles.add("V");
                }
                continue;
            }
            if (injector.isJsonPrimitive()) {
                String value = injector.getAsString().toUpperCase(Locale.ROOT);
                if (value.equals("V") || value.equals("A") || value.equals("M")) {
                    roles.add(value);
                }
            }
        }
        return roles;
    }
}
