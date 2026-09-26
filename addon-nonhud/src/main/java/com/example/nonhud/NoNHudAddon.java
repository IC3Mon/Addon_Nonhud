package com.example.nonhud;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoNHudAddon implements ModInitializer {
    public static final String MOD_ID = "nonhud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String NON_ANIM_STARTED_PACKET = "non_anim_started";
    public static final String NON_ANIM_STAGE_PACKET = "non_anim_stage";
    public static final String NON_ANIM_STOPPED_PACKET = "non_anim_stopped";
    public static final String NON_SLOT_SHOW_PACKET = "non_slot_show";
    public static final String NON_SLOT_HIDE_PACKET = "non_slot_hide";

    @Override
    public void onInitialize() {
        LOGGER.info("[NoNHudAddon] initializing...");
        // 步骤 7：首次启动"仅补缺"解压内置默认 NoNHud/ 示例图集（设计 §10），先于索引扫描
        com.example.nonhud.FirstRunExtractor.extract();
        // 外部 NoNHud/ 目录索引（O1 只读共享）
        com.example.nonhud.NoNHudRuntime.init();
        // 服务端动画事件监听（把 NoN 动画事件转成 S2C 包下发客户端）
        com.example.nonhud.server.NoNAnimationListener.onInitializeServer();
        // 槽位状态 S2C 载荷（专用服务器环境注册发送侧）
        com.example.nonhud.net.SlotSyncPacket.registerServer();
        // 触发引擎：event 槽（动画事件）+ numeric 槽（数值采样）
        com.example.nonhud.engine.EventSlotListener.register();
        com.example.nonhud.engine.NumericMonitor.register();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}