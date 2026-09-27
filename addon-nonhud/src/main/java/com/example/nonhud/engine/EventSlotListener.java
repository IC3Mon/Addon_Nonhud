package com.example.nonhud.engine;

import com.example.nonhud.ChatDebug;
import com.example.nonhud.NoNHudAddon;
import com.example.nonhud.NoNHudRuntime;
import com.example.nonhud.net.SlotSyncPacket;
import com.example.nonhud.net.SlotSyncPacket.SlotHidePayload;
import com.example.nonhud.net.SlotSyncPacket.SlotShowPayload;
import com.example.nonhud.scan.EventTier;
import com.example.nonhud.scan.Gender;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;
import com.nonid.api.NonGender;
import com.nonid.api.NonPlayerApi;
import com.nonid.api.animation.NonAnimationApi;
import com.nonid.api.animation.NonAnimationDefinition;
import com.nonid.api.animation.NonAnimationEvents;
import com.nonid.api.animation.NonAnimationStage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resource.Resource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 模块 1：event 槽触发适配层（服务端）。
 *
 * 监听 NoN 动画事件（STARTED/STAGE_CHANGED/STOPPED），把每个目标玩家的动画状态
 * 归约为 event 槽（A/V/M/botle/birth cue）档位，驱动 TriggerEngine 并 S2C 下发：
 * - 槽位判定：animdef 的 actors[].injector 角色（AnimdefParser 解析 JSON）+ A 槽性别强判
 *   （EventSlotResolver，设计 §3.3）；
 * - 峰档判定：stage 序号 vs 峰值（峰值按「最后一个 stage」近似，CHECKLIST §2.7）；
 * - botle：animdef content_tags 含 "botle" → 单档（无独立回调，按事件时机捕获）；
 * - birth cue：animdef system_tags 含 "birth" → pregnant 槽出生图集（设计 §3.3(b)）。
 */
public final class EventSlotListener {

    private static final EnumSet<Slot> EVENT_SLOTS = EnumSet.of(Slot.A, Slot.V, Slot.M, Slot.BOTLE);

    /** 触发 botle 槽的 NoN 动画 ID（模块 5.1 可配置；空 = 仅按 content_tags 判定）。 */
    private static volatile String fillBottleAnimId = "player_fill_bottle";

    public static void setFillBottleAnimId(String id) {
        fillBottleAnimId = id == null ? "" : id;
    }

    private EventSlotListener() { }

    public static void register() {
        // 动画开始：带当前 stage
        NonAnimationEvents.STARTED.register(ctx ->
                safeOnAnimation(ctx.world(), ctx.session().animationId(),
                        ctx.session().actorUuids(), ctx.requester(), ctx.session().currentStage(), "STARTED"));

        // 阶段切换
        NonAnimationEvents.STAGE_CHANGED.register(ctx ->
                safeOnAnimation(ctx.world(), ctx.animationId(), ctx.actorUuids(), null, ctx.currentStage(), "STAGE_CHANGED"));

        // 动画结束：隐藏 event 槽；出生 cue 结束则恢复妊娠进度分档
        NonAnimationEvents.STOPPED.register(ctx ->
                safeOnStopped(ctx.world(), ctx.animationId(), ctx.actorUuids()));
    }

    /** 事件回调加固：异常不中断 NoN 动画系统，转入聊天调试消息提示。 */
    private static void safeOnAnimation(ServerWorld world, Identifier animId, List<UUID> actorUuids,
                                        ServerPlayerEntity requester, NonAnimationStage stage, String event) {
        try {
            onAnimation(world, animId, actorUuids, requester, stage, event);
        } catch (RuntimeException e) {
            NoNHudAddon.LOGGER.error("[NoNHudAddon] EventSlotListener {} failed for {}", event, animId, e);
            if (requester != null) {
                ChatDebug.server(requester, "动画处理异常: " + e);
            } else if (!actorUuids.isEmpty()) {
                ServerPlayerEntity p = world.getServer().getPlayerManager().getPlayer(actorUuids.get(0));
                ChatDebug.server(p, "动画处理异常: " + e);
            }
        }
    }

    private static void safeOnStopped(ServerWorld world, Identifier animId, List<UUID> actorUuids) {
        try {
            onStopped(world, animId, actorUuids);
        } catch (RuntimeException e) {
            NoNHudAddon.LOGGER.error("[NoNHudAddon] EventSlotListener STOPPED failed for {}", animId, e);
            if (!actorUuids.isEmpty()) {
                ChatDebug.server(world.getServer().getPlayerManager().getPlayer(actorUuids.get(0)),
                        "动画结束处理异常: " + e);
            }
        }
    }

    private static void onAnimation(ServerWorld world, Identifier animId, List<UUID> actorUuids,
                                    ServerPlayerEntity requester, NonAnimationStage stage, String event) {
        NonAnimationDefinition def = NonAnimationApi.getDefinition(animId);
        if (def == null) {
            ChatDebug.server(requester, "[" + event + "] 动画 " + animId + " 未找到定义，跳过槽位计算");
            return;
        }
        int stageIndex = def.stages().indexOf(stage);
        if (stageIndex < 0) {
            stageIndex = 0;
        }
        EventTier tier = StageTier.tierForStage(stageIndex, def.stages().size() - 1);

        boolean botle = containsIgnoreCase(def.contentTags(), "botle")
                || matchesFillBottleAnimId(animId);
        boolean birth = containsIgnoreCase(def.systemTags(), "birth");
        Set<String> injectorRoles = loadInjectorRoles(world, animId);
        ChatDebug.server(requester, "[" + event + "] 动画 " + animId + " stage=" + (stageIndex + 1)
                + "/" + def.stages().size() + " 档位=" + tier
                + " injector=" + injectorRoles + " botle=" + botle + " birth=" + birth);

        Set<UUID> targets = new LinkedHashSet<>(actorUuids);
        if (requester != null) {
            targets.add(requester.getUuid());
        }
        for (UUID uuid : targets) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null) {
                continue;
            }
            if (birth) {
                showBirth(player);
                continue;
            }
            Gender gender = genderOf(NonPlayerApi.getGender(player));
            Set<EventSlotResolver.Resolved> resolvedSlots =
                    EventSlotResolver.resolve(injectorRoles, botle, gender);
            if (resolvedSlots.isEmpty()) {
                ChatDebug.server(player, "[" + event + "] " + animId
                        + " 未匹配事件槽（injector=" + injectorRoles + "）→ 不触发 HUD");
                continue;
            }
            TriggerEngine engine = TriggerEngineHub.getFor(uuid);
            for (EventSlotResolver.Resolved resolved : resolvedSlots) {
                SlotKey key = switch (resolved.slot()) {
                    case A -> SlotKey.eventA(resolved.gender(), tier);
                    case BOTLE -> SlotKey.botle();
                    default -> SlotKey.event(resolved.slot(), tier);
                };
                engine.show(resolved.slot(), key, NoNHudRuntime.index().get(key));
                SlotKey shown = engine.current(resolved.slot())
                        .map(TriggerEngine.Displayed::key).orElse(null);
                if (shown != null) {
                    ChatDebug.server(player, "[" + event + "] 显示槽位 " + resolved.slot() + " 档位 " + shown.phaseKey()
                            + " 候选数=" + NoNHudRuntime.index().get(key).size());
                    ServerPlayNetworking.send(player, new SlotShowPayload(shown.phaseKey()));
                } else {
                    ChatDebug.server(player, "[" + event + "] 槽位 " + resolved.slot()
                            + " 图集无候选（phaseKey=" + key.phaseKey() + "）→ 隐藏");
                    ServerPlayNetworking.send(player, new SlotHidePayload(resolved.slot().name()));
                }
            }
        }
    }

    private static void onStopped(ServerWorld world, Identifier animId, List<UUID> actorUuids) {
        NonAnimationDefinition def = NonAnimationApi.getDefinition(animId);
        boolean birth = def != null && containsIgnoreCase(def.systemTags(), "birth");
        for (UUID uuid : actorUuids) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null) {
                continue;
            }
            TriggerEngine engine = TriggerEngineHub.getFor(uuid);
            if (birth) {
                // 出生 cue 结束：先隐藏出生图集，再恢复妊娠进度分档显示
                engine.hide(Slot.PREGNANT);
                ChatDebug.server(player, "[STOPPED] 动画 " + animId + " 结束，隐藏 pregnant");
                ServerPlayNetworking.send(player, new SlotHidePayload(Slot.PREGNANT.name()));
                NumericMonitor.resamplePregnancy(player);
                continue;
            }
            for (Slot slot : EVENT_SLOTS) {
                if (engine.isActive(slot)) {
                    engine.hide(slot);
                    ChatDebug.server(player, "[STOPPED] 动画 " + animId + " 结束，隐藏 " + slot);
                    ServerPlayNetworking.send(player, new SlotHidePayload(slot.name()));
                }
            }
        }
    }

    /** 出生 cue：pregnant 槽显示出生图集（高优先级覆盖进度分档）。 */
    private static void showBirth(ServerPlayerEntity player) {
        SlotKey birthKey = SlotKey.birth();
        TriggerEngine engine = TriggerEngineHub.getFor(player.getUuid());
        engine.show(Slot.PREGNANT, birthKey, NoNHudRuntime.index().get(birthKey));
        SlotKey shown = engine.current(Slot.PREGNANT)
                .map(TriggerEngine.Displayed::key).orElse(null);
        if (shown != null) {
            ServerPlayNetworking.send(player, new SlotShowPayload(shown.phaseKey()));
        }
    }

    /** 从资源包读 animdef JSON 提取 injector 角色；读取/解析失败 → 空集（容忍）。 */
    private static Set<String> loadInjectorRoles(ServerWorld world, Identifier animId) {
        try {
            Resource resource = world.getServer().getResourceManager()
                    .getResource(Identifier.of(animId.getNamespace(),
                            "afw_animdefs/" + animId.getPath() + ".json"))
                    .orElse(null);
            if (resource == null) {
                return Set.of();
            }
            try (InputStream in = resource.getInputStream()) {
                return AnimdefParser.injectorRoles(
                        new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (IOException | RuntimeException e) {
            return Set.of();
        }
    }

    private static boolean containsIgnoreCase(Collection<String> tags, String needle) {
        return tags.stream().anyMatch(t -> t.equalsIgnoreCase(needle));
    }

    /** 模块 5.1：动画 ID 匹配配置的 fillBottleAnimId（支持 "namespace:path" 或 "path" 写法；
     * 另按不区分大小写的子串匹配，兼容命名差异，如 player_fill_bottle / player_full_bottle）。 */
    private static boolean matchesFillBottleAnimId(Identifier animId) {
        String id = fillBottleAnimId;
        if (id.isBlank()) {
            return false;
        }
        String path = animId.getPath();
        String full = animId.toString();
        return id.equals(path) || id.equals(full)
                || path.toLowerCase(java.util.Locale.ROOT).contains(id.toLowerCase(java.util.Locale.ROOT));
    }

    /** NonGender → 目录性别档：MALE→male，其余（FEMALE/BOTH/NONE）→ female。 */
    private static Gender genderOf(NonGender g) {
        return g == NonGender.MALE ? Gender.MALE : Gender.FEMALE;
    }
}
