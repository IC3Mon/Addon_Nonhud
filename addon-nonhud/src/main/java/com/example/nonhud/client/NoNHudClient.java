package com.example.nonhud.client;

import com.example.nonhud.NoNHudAddon;
import net.fabricmc.api.ClientModInitializer;

public class NoNHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NoNHudAddon.LOGGER.info("[NoNHudAddon] client init...");

        // 1) 注册 HUD 渲染回调：模块 4 渲染器（消费 SlotDisplayRegistry 快照，按 Z 序多槽绘制）
        HudRenderer.INSTANCE.register();

        // 2) 注册动画状态同步网络包接收（把服务端下发的“动画+stage”写入内存）
        AnimationStateSync.registerReceivers();

        // 3) 注册槽位状态同步接收（触发引擎 → SlotDisplayRegistry，模块 4 渲染器消费）
        com.example.nonhud.net.SlotSyncPacket.registerReceivers();

        // 4) 模块 5：加载 config.json（O3/O4：缺省落盘 + 应用进渲染/阈值/索引）
        com.example.nonhud.config.ConfigManagers.INSTANCE.load(true);

        // 5) 模块 5：快捷键（暂停 N1 / 设置界面）
        com.example.nonhud.config.ConfigKeybinds.register();
    }
}