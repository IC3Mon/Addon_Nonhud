package com.example.nonhud.client;

import com.example.nonhud.ChatDebug;
import com.example.nonhud.NoNHudAddon;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * 客户端：接收服务端下发的动画状态同步包（原始动画事件观测）。
 *
 * 说明：槽位 HUD 弹图由 SlotSyncPacket → SlotDisplayRegistry 管线驱动；
 * 本类承载的原始 (动画ID, stage) 包仅用于在聊天窗口输出调试消息
 * （诊断"播放动画时不弹图"时对照服务端事件与客户端收到的原始包）。
 */
public final class AnimationStateSync {

    private AnimationStateSync() { }

    /** 动画开始：携带动画 ID 与起始 stage。 */
    public record AnimStartedPayload(String anim, int stage) implements CustomPayload {
        public static final CustomPayload.Id<AnimStartedPayload> ID =
                new CustomPayload.Id<>(NoNHudAddon.id(NoNHudAddon.NON_ANIM_STARTED_PACKET));
        public static final PacketCodec<RegistryByteBuf, AnimStartedPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, AnimStartedPayload::anim,
                PacketCodecs.VAR_INT, AnimStartedPayload::stage,
                AnimStartedPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 阶段切换：携带动画 ID 与新的 stage。 */
    public record AnimStagePayload(String anim, int stage) implements CustomPayload {
        public static final CustomPayload.Id<AnimStagePayload> ID =
                new CustomPayload.Id<>(NoNHudAddon.id(NoNHudAddon.NON_ANIM_STAGE_PACKET));
        public static final PacketCodec<RegistryByteBuf, AnimStagePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, AnimStagePayload::anim,
                PacketCodecs.VAR_INT, AnimStagePayload::stage,
                AnimStagePayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 动画结束：清空 HUD 图。 */
    public record AnimStoppedPayload() implements CustomPayload {
        public static final CustomPayload.Id<AnimStoppedPayload> ID =
                new CustomPayload.Id<>(NoNHudAddon.id(NoNHudAddon.NON_ANIM_STOPPED_PACKET));
        public static final PacketCodec<RegistryByteBuf, AnimStoppedPayload> CODEC =
                PacketCodec.unit(new AnimStoppedPayload());
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void registerReceivers() {
        // 注册 S2C 载荷类型
        PayloadTypeRegistry.playS2C().register(AnimStartedPayload.ID, AnimStartedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnimStagePayload.ID, AnimStagePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnimStoppedPayload.ID, AnimStoppedPayload.CODEC);

        // 播放开始
        ClientPlayNetworking.registerGlobalReceiver(AnimStartedPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        ChatDebug.client("收到动画原始包: 开始 anim=" + payload.anim() + " stage=" + payload.stage())));

        // 阶段切换
        ClientPlayNetworking.registerGlobalReceiver(AnimStagePayload.ID, (payload, context) ->
                context.client().execute(() ->
                        ChatDebug.client("收到动画原始包: 阶段 anim=" + payload.anim() + " stage=" + payload.stage())));

        // 播放结束
        ClientPlayNetworking.registerGlobalReceiver(AnimStoppedPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        ChatDebug.client("收到动画原始包: 结束")));
    }
}
