package com.example.nonhud.net;

import com.example.nonhud.ChatDebug;
import com.example.nonhud.NoNHudAddon;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * 服务端 → 客户端槽位状态同步包（触发引擎驱动 HUD 的通道）。
 *
 * - SlotShowPayload(phaseKey)：某槽显示某档位（phaseKey = SlotKey.phaseKey()，客户端还原 SlotKey）。
 * - SlotHidePayload(slot)：某槽隐藏。
 *
 * 注册方式与 AnimationStateSync 一致：客户端注册 S2C 类型 + 全局接收器；
 * 专用服务器环境才注册发送侧类型（集成服复用客户端注册，避免重复注册冲突）。
 */
public final class SlotSyncPacket {

    private SlotSyncPacket() { }

    /** 槽位显示：携带 SlotKey.phaseKey()。 */
    public record SlotShowPayload(String phaseKey) implements CustomPayload {
        public static final CustomPayload.Id<SlotShowPayload> ID =
                new CustomPayload.Id<>(NoNHudAddon.id(NoNHudAddon.NON_SLOT_SHOW_PACKET));
        public static final PacketCodec<RegistryByteBuf, SlotShowPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, SlotShowPayload::phaseKey,
                SlotShowPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 槽位隐藏：携带槽位枚举名。 */
    public record SlotHidePayload(String slot) implements CustomPayload {
        public static final CustomPayload.Id<SlotHidePayload> ID =
                new CustomPayload.Id<>(NoNHudAddon.id(NoNHudAddon.NON_SLOT_HIDE_PACKET));
        public static final PacketCodec<RegistryByteBuf, SlotHidePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, SlotHidePayload::slot,
                SlotHidePayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 专用服务器环境注册发送侧载荷类型（集成服由客户端注册）。 */
    public static void registerServer() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            PayloadTypeRegistry.playS2C().register(SlotShowPayload.ID, SlotShowPayload.CODEC);
            PayloadTypeRegistry.playS2C().register(SlotHidePayload.ID, SlotHidePayload.CODEC);
        }
    }

    /** 客户端：注册 S2C 载荷类型与接收器，写入 SlotDisplayRegistry。 */
    public static void registerReceivers() {
        PayloadTypeRegistry.playS2C().register(SlotShowPayload.ID, SlotShowPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SlotHidePayload.ID, SlotHidePayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(SlotShowPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    ChatDebug.client("收到槽位显示 phaseKey=" + payload.phaseKey());
                    com.example.nonhud.client.SlotDisplayRegistry.INSTANCE.show(payload.phaseKey());
                }));

        ClientPlayNetworking.registerGlobalReceiver(SlotHidePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    ChatDebug.client("收到槽位隐藏 slot=" + payload.slot());
                    com.example.nonhud.client.SlotDisplayRegistry.INSTANCE.hide(payload.slot());
                }));
    }
}
