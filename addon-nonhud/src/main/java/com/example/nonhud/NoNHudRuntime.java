package com.example.nonhud;

import com.example.nonhud.scan.DirectoryScanner;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotIndex;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Set;

/**
 * 模块 0/1/2/3 共享运行时：定位外部 NoNHud/ 根目录，持有只读槽索引（O1 重建后引用替换）。
 *
 * 服务端适配层（EventSlotListener / NumericMonitor）在此取图集候选；
 * 客户端渲染（模块 4）同样读取本索引。单机集成服下两侧共享同一目录。
 */
public final class NoNHudRuntime {

    private static volatile SlotIndex index = SlotIndex.of(java.util.Map.of());
    private static Path root;
    private static boolean initialized;

    private NoNHudRuntime() { }

    public static void init() {
        root = FabricLoader.getInstance().getGameDir().resolve("NoNHud");
        reload();
        initialized = true;
        NoNHudAddon.LOGGER.info("[NoNHudAddon] NoNHud root: {}", root);
    }

    /** 重建索引（O1）：整体替换不可变索引引用；缺失目录 → 空索引，不报错。 */
    public static void reload() {
        reload(null);
    }

    /** 重建索引并应用槽位开关（O4：模块 5 配置热重载调用；enabled 为空 = 全部启用）。 */
    public static void reload(Set<Slot> enabled) {
        if (root == null) {
            return; // 未 init（单测环境）
        }
        index = DirectoryScanner.scan(root, enabled);
        NoNHudAddon.LOGGER.info("[NoNHudAddon] indexed {} slot-phase keys", index.size());
    }

    public static SlotIndex index() {
        return index;
    }

    public static Path root() {
        if (root == null) {
            root = FabricLoader.getInstance().getGameDir().resolve("NoNHud");
        }
        return root;
    }

    public static boolean initialized() {
        return initialized;
    }
}
