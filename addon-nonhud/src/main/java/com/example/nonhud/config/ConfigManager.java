package com.example.nonhud.config;

import com.example.nonhud.NoNHudRuntime;
import com.example.nonhud.engine.NumericMonitor;
import com.example.nonhud.render.MoveMode;
import com.example.nonhud.render.RenderDefaults;
import com.example.nonhud.render.ScaleMode;
import com.example.nonhud.scan.Slot;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 模块 5 配置管理器（O3 热重载 / O4 槽位开关）：
 * - 从 NoNHud/config.json 读取（缺文件 → 生成默认并落盘）；
 * - apply()：把配置写入 RenderDefaults（渲染侧每帧读取）+ 注入 NumericMonitor 阈值
 *   + 槽位开关经 NoNHudRuntime.reload(slotsEnabled) 重建索引（O1）；
 * - save()：当前 Config 写回文件。
 *
 * 纯文件逻辑，构造注入路径以便单测；INSTANCE 供运行期使用。
 */
public final class ConfigManager {

    public static final String FILE_NAME = "config.json";

    private final Path file;
    private Config current;

    public ConfigManager(Path root) {
        this.file = root.resolve(FILE_NAME);
    }

    public Path file() {
        return file;
    }

    public Config current() {
        return current == null ? new Config() : current;
    }

    /**
     * 加载并落盘默认（若文件缺失/损坏），随后应用。
     * @param overwrite 是否强制用当前值重写文件（设为 false 时损坏文件→默认但保留原文件）
     */
    public void load(boolean overwrite) {
        if (Files.isRegularFile(file)) {
            try {
                String json = Files.readString(file, StandardCharsets.UTF_8);
                current = ConfigCodec.fromJson(json);
            } catch (JsonParseException | IOException e) {
                // 损坏配置：回退默认，不破坏原文件（O3 容错）
                current = new Config();
                if (overwrite) {
                    save();
                }
            }
        } else {
            current = new Config();
            save();
        }
        apply();
    }

    /** 保存当前配置到文件（父目录不存在则创建）。 */
    public void save() {
        try {
            Path parent = file.getParent();
            if (parent != null && !Files.isDirectory(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, ConfigCodec.toJson(current()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 写失败不致命：配置仅内存生效，下次启动重试
        }
    }

    /** 把当前配置应用进渲染/触发/索引（O3 热重载）。 */
    public void apply() {
        Config c = current();
        RenderDefaults.setFps(c.fps);
        RenderDefaults.setZOrder(c.zOrder);
        RenderDefaults.setFadeInMs(c.fadeInMs);
        RenderDefaults.setFadeOutMs(c.fadeOutMs);
        RenderDefaults.setScaleMode(c.scaleMode);
        RenderDefaults.setScalePercent(c.scalePercent);
        RenderDefaults.setNumericDisplayMs(c.numericDurationMs);
        for (Map.Entry<Slot, com.example.nonhud.render.Region> e : c.regions.entrySet()) {
            RenderDefaults.setRegion(e.getKey(), e.getValue());
        }
        for (Map.Entry<Slot, MoveMode> e : c.moveModes.entrySet()) {
            RenderDefaults.setMoveMode(e.getKey(), e.getValue());
        }
        NumericMonitor.setThresholds(c.energyThresholds, c.vTankThresholds,
                c.aTankThresholds, c.pregnancyThresholds);
        NumericMonitor.setDisplayDurationMs(c.numericDurationMs);
        // 槽位开关 → 重建索引（O4/O1）
        NoNHudRuntime.reload(c.slotsEnabled);
    }

    /** 用给定值替换当前配置并保存 + 应用（设置界面保存入口）。 */
    public void updateAndApply(Config next) {
        this.current = next;
        save();
        apply();
    }
}
