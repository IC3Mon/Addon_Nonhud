package com.example.nonhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * 聊天调试消息工具：在关键链路节点把诊断信息输出到游戏聊天窗口。
 *
 * - 服务端侧：{@link #server(ServerPlayerEntity, String)} —— 专用服务器/集成服均可，发给指定玩家；
 * - 客户端侧：{@link #client(String)} —— 发给当前玩家（主渲染线程调用，无玩家时仅记日志）。
 * 统一前缀 [NoNHud]，同时写入 LOGGER 便于查日志。
 */
public final class ChatDebug {

    private static final String PREFIX = "[NoNHud] ";

    private ChatDebug() { }

    /** 服务端 → 玩家聊天窗口（false = 仅聊天栏，不占动作栏）。 */
    public static void server(ServerPlayerEntity player, String message) {
        if (player == null) {
            return;
        }
        player.sendMessage(Text.literal(PREFIX + message), false);
        NoNHudAddon.LOGGER.info(PREFIX + message);
    }

    /** 客户端 → 当前玩家聊天窗口；非客户端环境 / 未进世界时仅记日志。 */
    public static void client(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null) {
            mc.player.sendMessage(Text.literal(PREFIX + message), false);
        }
        NoNHudAddon.LOGGER.info(PREFIX + message);
    }
}
