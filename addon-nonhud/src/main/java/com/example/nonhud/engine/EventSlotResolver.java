package com.example.nonhud.engine;

import com.example.nonhud.scan.Gender;
import com.example.nonhud.scan.Slot;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 事件槽决策（设计文档 §3.3 A 槽性别强判 + §3.4 injector 角色）：
 *
 * 一个动画可能同时含多个 injector 角色（如 courin:3bee_fuck injector=[A,M,V]），
 * 全部角色对应的槽位应同时显示（多槽并行，客户端按 Z 序分别绘制）。
 *
 * 规则：
 * 1. botle（content_tags 含 "botle"）→ 仅单档 BOTLE（独占，不再展开其他角色）；
 * 2. injector 含 A → A 槽，取 receiver 性别子目录；
 * 3. injector 含 V → 男角色强制转 A/male（NoN male-only V→A 兼容转换，README 1424；
 *    与第 2 条结果经 set 去重）；女性取 V 槽；
 * 4. injector 含 M → M 槽；
 * 5. 无注入角色 → 空集（不触发）。
 */
public final class EventSlotResolver {

    /** 事件槽解析结果：一个角色对应一个槽位（空集 = 不触发）。 */
    public record Resolved(Slot slot, Gender gender) {
        public static Resolved slot(Slot slot, Gender gender) {
            return new Resolved(slot, gender);
        }
    }

    private EventSlotResolver() { }

    public static Set<Resolved> resolve(Set<String> injectorRoles, boolean botle, Gender playerGender) {
        Set<Resolved> out = new LinkedHashSet<>();
        if (botle) {
            out.add(Resolved.slot(Slot.BOTLE, null));
            return out;
        }
        if (injectorRoles.contains("A")) {
            out.add(Resolved.slot(Slot.A, playerGender));
        }
        if (injectorRoles.contains("V")) {
            if (playerGender == Gender.MALE) {
                out.add(Resolved.slot(Slot.A, Gender.MALE));
            } else {
                out.add(Resolved.slot(Slot.V, null));
            }
        }
        if (injectorRoles.contains("M")) {
            out.add(Resolved.slot(Slot.M, null));
        }
        return out;
    }
}
