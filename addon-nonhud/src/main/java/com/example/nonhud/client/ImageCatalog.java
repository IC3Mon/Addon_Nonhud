package com.example.nonhud.client;

import com.example.nonhud.NoNHudAddon;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * (animationId, stage) 到纹理资源的映射。
 * 把你想显示的剧情图放这里，key = "动画ID|stage"。
 *
 * 图片文件放在：src/main/resources/assets/nonhud/textures/ 下。
 * 例如 key 为 "needsofnature:xxx|3" 会去查 assets/nonhud/textures/xxx_3.png
 */
public final class ImageCatalog {
    private static final Map<String, Identifier> TABLE = new HashMap<>();

    static {
        // 示例：在动画 needsofnature:demo 的第 2 阶段显示 demo_stage2.png
        put("needsofnature:demo", 2, "nonhud", "textures/demo_stage2.png");
    }

    private ImageCatalog() { }

    private static void put(String animationId, int stage, String namespace, String path) {
        TABLE.put(animationId + "|" + stage, Identifier.of(namespace, path));
    }

    /** 返回该 (动画, 阶段) 对应纹理；没有则 null。 */
    public static Identifier imageFor(String key) {
        return TABLE.get(key);
    }
}