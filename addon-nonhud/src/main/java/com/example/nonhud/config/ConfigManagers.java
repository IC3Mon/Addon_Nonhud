package com.example.nonhud.config;

import com.example.nonhud.NoNHudRuntime;

/**
 * 运行期 ConfigManager 单例（NoNHud/config.json）。
 * 独立成类，避免 ConfigManager 本体在单测环境（无 FabricLoader）触发静态初始化。
 */
public final class ConfigManagers {

    public static final ConfigManager INSTANCE = new ConfigManager(NoNHudRuntime.root());

    private ConfigManagers() { }
}
