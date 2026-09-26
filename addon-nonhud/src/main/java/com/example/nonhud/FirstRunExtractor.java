package com.example.nonhud;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * 步骤 7 首次启动初始化（设计文档 §10 打包与首次启动解压）：
 * 把 jar 内置默认 NoNHud/ 示例图集"仅补缺"释放到游戏目录。
 *
 * - 源：jar 内 assets/nonhud/default/NoNHud/（树结构与外部一致，§11.1）
 * - 目标：gameDir/NoNHud/（NoNHudRuntime.root()）
 * - 策略：仅补缺、不覆盖——目标已存在的文件一律保留（玩家内容优先，§10.2）
 * - config.json 不在解压范围：由 ConfigManager 缺省生成（避免双默认值漂移）
 */
public final class FirstRunExtractor {

    /** jar 内默认资源根（相对 classpath）。 */
    public static final String DEFAULT_ROOT = "assets/nonhud/default/NoNHud";

    private FirstRunExtractor() { }

    /** 运行期入口：mod jar → gameDir/NoNHud/（仅补缺）。由 NoNHudAddon.onInitialize 调用。 */
    public static void extract() {
        ModContainer container = FabricLoader.getInstance()
                .getModContainer(NoNHudAddon.MOD_ID).orElse(null);
        if (container == null) {
            return;
        }
        Path source = container.findPath(DEFAULT_ROOT).orElse(null);
        if (source == null) {
            NoNHudAddon.LOGGER.warn("[NoNHudAddon] built-in NoNHud resource missing: {}", DEFAULT_ROOT);
            return;
        }
        try {
            copyMissingRecursively(source, NoNHudRuntime.root());
            NoNHudAddon.LOGGER.info("[NoNHudAddon] first-run extract done (fill-missing only) -> {}",
                    NoNHudRuntime.root());
        } catch (IOException e) {
            NoNHudAddon.LOGGER.warn("[NoNHudAddon] first-run extract failed", e);
        }
    }

    /**
     * 把 source 目录树复制到 target，仅补缺（target 已存在的文件不覆盖）。
     * 纯文件逻辑，不依赖 MC 运行时，供单元测试与运行期共用。
     */
    public static void copyMissingRecursively(Path source, Path target) throws IOException {
        if (!Files.isDirectory(source)) {
            return;
        }
        List<Path> files;
        try (Stream<Path> walk = Files.walk(source)) {
            files = walk.filter(Files::isRegularFile).toList();
        }
        for (Path f : files) {
            Path dst = target.resolve(source.relativize(f));
            if (Files.exists(dst)) {
                continue; // 仅补缺：保留玩家已有文件
            }
            Path parent = dst.getParent();
            if (parent != null && !Files.isDirectory(parent)) {
                Files.createDirectories(parent);
            }
            Files.copy(f, dst);
        }
    }
}
