package com.example.nonhud.server;

import com.example.nonhud.ChatDebug;
import com.example.nonhud.NoNHudAddon;
import com.example.nonhud.client.AnimationStateSync.AnimStagePayload;
import com.example.nonhud.client.AnimationStateSync.AnimStartedPayload;
import com.example.nonhud.client.AnimationStateSync.AnimStoppedPayload;
import com.nonid.api.animation.NonAnimationApi;
import com.nonid.api.animation.NonAnimationDefinition;
import com.nonid.api.animation.NonAnimationEvents;
import com.nonid.api.animation.NonAnimationSession;
import com.nonid.api.animation.NonAnimationStage;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 服务端监听 NoN 动画事件，把 (动画ID, stage序号) 以 S2C 载荷下发给相关玩家，驱动客户端 HUD 弹图。
 *
 * 真实 API 结论（javap 核对 needsofnature-1.5.1+1.21.11.jar）：
 * - com.nonid.api.animation.NonAnimationEvents：BEFORE_START / STARTED / STAGE_CHANGED / STOPPED
 * - StartedContext(world, session, requester)；session.animationId()/currentStage()/actorUuids()
 * - StageChangedContext(world, instanceId, animationId, actorUuids, actorKeys, advanceTick, currentStage)
 * - StoppedContext(world, instanceId, animationId, actorUuids, actorKeys, stages)
 * - stage 不是 int：stage 序号 = NonAnimationApi.getDefinition(animationId).stages().indexOf(currentStage)
 *   （NonAnimationStage 为 record，可 equals 比较）
 */
public final class NoNAnimationListener {

    private NoNAnimationListener() { }

    /**
     * 注册服务端动画事件监听与 S2C 载荷类型。调用入口：NoNHudAddon.onInitialize()。
     * 客户端已注册接收（AnimationStateSync.registerReceivers），此处仅在专用服务器环境注册
     * 发送侧载荷类型，避免与客户端重复注册冲突（集成服务器复用客户端的注册）。
     */
    public static void onInitializeServer() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            PayloadTypeRegistry.playS2C().register(AnimStartedPayload.ID, AnimStartedPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(AnimStagePayload.ID, AnimStagePayload.CODEC);
            PayloadTypeRegistry.playS2C().register(AnimStoppedPayload.ID, AnimStoppedPayload.CODEC);
        }

        // 动画开始：带 (动画ID, 起始 stage)，发给请求者 + 参与者
        NonAnimationEvents.STARTED.register(ctx -> {
            try {
                NonAnimationSession session = ctx.session();
                String anim = session.animationId().toString();
                int stage = stageIndex(ctx.world(), session.animationId(), session.currentStage());
                Set<UUID> targets = new LinkedHashSet<>(session.actorUuids());
                if (ctx.requester() != null) {
                    targets.add(ctx.requester().getUuid());
                }
                sendToPlayers(ctx.world(), targets, new AnimStartedPayload(anim, stage),
                        "动画开始 " + anim + " stage=" + stage);
            } catch (RuntimeException e) {
                NoNHudAddon.LOGGER.error("[NoNHudAddon] NoNAnimationListener STARTED failed", e);
            }
        });

        // 阶段切换：带 (动画ID, 新 stage)，发给参与者
        NonAnimationEvents.STAGE_CHANGED.register(ctx -> {
            try {
                int stage = stageIndex(ctx.world(), ctx.animationId(), ctx.currentStage());
                sendToPlayers(ctx.world(), new LinkedHashSet<>(ctx.actorUuids()),
                        new AnimStagePayload(ctx.animationId().toString(), stage),
                        "动画阶段 " + ctx.animationId() + " stage=" + stage);
            } catch (RuntimeException e) {
                NoNHudAddon.LOGGER.error("[NoNHudAddon] NoNAnimationListener STAGE_CHANGED failed", e);
            }
        });

        // 动画结束：清空 HUD，发给参与者
        NonAnimationEvents.STOPPED.register(ctx -> {
            try {
                sendToPlayers(ctx.world(), new LinkedHashSet<>(ctx.actorUuids()), new AnimStoppedPayload(),
                        "动画结束 " + ctx.animationId());
            } catch (RuntimeException e) {
                NoNHudAddon.LOGGER.error("[NoNHudAddon] NoNAnimationListener STOPPED failed", e);
            }
        });
    }

    /** stage 序号：以定义文件的 stage 列表下标为准；查不到按 0 处理。 */
    private static int stageIndex(ServerWorld world, Identifier animId, NonAnimationStage stage) {
        NonAnimationDefinition def = NonAnimationApi.getDefinition(animId);
        if (def == null) {
            NoNHudAddon.LOGGER.warn("[NoNHudAddon] animation {} not loaded, default stage=0", animId);
            return 0;
        }
        int idx = def.stages().indexOf(stage);
        return idx < 0 ? 0 : idx;
    }

    /** 把载荷发给目标玩家（同时向目标输出聊天调试消息）；目标为空则向世界内所有玩家广播。 */
    private static void sendToPlayers(ServerWorld world, Set<UUID> targets, CustomPayload payload, String debugMsg) {
        Collection<ServerPlayerEntity> players = world.getPlayers();
        if (targets.isEmpty()) {
            players.forEach(p -> {
                ChatDebug.server(p, debugMsg);
                ServerPlayNetworking.send(p, payload);
            });
            return;
        }
        players.stream()
                .filter(p -> targets.contains(p.getUuid()))
                .forEach(p -> {
                    ChatDebug.server(p, debugMsg);
                    ServerPlayNetworking.send(p, payload);
                });
    }
}
