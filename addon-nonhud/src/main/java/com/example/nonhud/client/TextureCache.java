package com.example.nonhud.client;

import com.example.nonhud.ChatDebug;
import com.example.nonhud.NoNHudAddon;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 外部图片 → 纹理缓存（O2：只装载当前阶段需要的帧，切帧/切槽释放）。
 *
 * 每个已装载的图片对应一个 {@link NativeImageBackedTexture}，注册进 TextureManager，
 * 渲染器经其 Identifier 绘制。同一路径只装载一次（帧序列多帧共享）。
 * 移除引用时销毁纹理并 close 图像（释放显存/内存）。
 *
 * 纯客户端薄胶水：不参与帧推进/淡入淡出等逻辑（见 SlotRenderState）。
 */
public final class TextureCache {

    /** 纹理信息：注册后的 Identifier + 图片原始尺寸。 */
    public record Loaded(Identifier id, int width, int height) { }

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final String NAMESPACE = "nonhud";

    /**
     * 装载纹理的最大边长（像素）。HUD 图集原图可能远超 GPU 纹理上限
     * （如 2499x4148 > 常见的 4096），超出会导致 glTexImage2D 失败、纹理为空，
     * 表现为"播放图片但屏幕上不显示"。HUD 实际显示仅约 90 像素，缩到该尺寸内即可。
     */
    static final int MAX_TEXTURE_EDGE = 1024;

    private final TextureManager textures;
    private final Map<Path, Loaded> cache = new HashMap<>();

    public TextureCache(TextureManager textures) {
        this.textures = textures;
    }

    /**
     * 装载图片文件为纹理；文件缺失/损坏或纹理管理器未就绪则返回 null（设计 §2.3：容忍异常图片，静默跳过）。
     * 超大图先按最近邻缩略到 {@link #MAX_TEXTURE_EDGE} 内再上传，避免超出 GPU 纹理上限。
     */
    public Loaded load(Path file) {
        if (textures == null) {
            String msg = "纹理管理器未就绪(null) → 无法装载: " + file;
            ChatDebug.client(msg);
            NoNHudAddon.LOGGER.warn("[NoNHudAddon] texture load failed: {} (texture manager is null)", file);
            return null;
        }
        Loaded hit = cache.get(file);
        if (hit != null) {
            return hit;
        }
        if (file == null) {
            NoNHudAddon.LOGGER.warn("[NoNHudAddon] texture load failed: file path is null");
            return null;
        }
        if (!Files.isRegularFile(file)) {
            String msg = "非普通文件/不存在: " + file
                    + " (exists=" + Files.exists(file) + ", isRegularFile=false)";
            ChatDebug.client(msg);
            NoNHudAddon.LOGGER.warn("[NoNHudAddon] texture load failed: {} (not a regular file, exists={})",
                    file, Files.exists(file));
            return null;
        }
        try (InputStream in = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            if (Math.max(image.getWidth(), image.getHeight()) > MAX_TEXTURE_EDGE) {
                NativeImage scaled = clampToMaxEdge(image, MAX_TEXTURE_EDGE);
                image.close();
                image = scaled;
            }
            Identifier id = Identifier.of(NAMESPACE, "dyn/" + SEQUENCE.incrementAndGet());
            NativeImageBackedTexture tex = new NativeImageBackedTexture(file::toString, image);
            textures.registerTexture(id, tex);
            Loaded loaded = new Loaded(id, image.getWidth(), image.getHeight());
            cache.put(file, loaded);
            return loaded;
        } catch (Throwable t) {
            String msg = "纹理装载异常: " + t.getClass().getSimpleName() + ": " + t.getMessage();
            ChatDebug.client(msg);
            NoNHudAddon.LOGGER.warn("[NoNHudAddon] texture load failed: {} ({})", file, t.toString());
            return null; // 静默跳过：非 .jpg/.png 或损坏图片（详情见日志）
        }
    }

    /** 最近邻缩略：把图片最大边压到 maxEdge 内，返回新图（调用方负责 close 原图）。 */
    static NativeImage clampToMaxEdge(NativeImage src, int maxEdge) {
        int w = src.getWidth();
        int h = src.getHeight();
        int m = Math.max(w, h);
        if (m <= maxEdge) {
            return src;
        }
        double s = (double) maxEdge / m;
        int nw = Math.max(1, (int) Math.round(w * s));
        int nh = Math.max(1, (int) Math.round(h * s));
        NativeImage out = new NativeImage(nw, nh, true);
        for (int x = 0; x < nw; x++) {
            int sx = Math.min(w - 1, x * w / nw);
            for (int y = 0; y < nh; y++) {
                int sy = Math.min(h - 1, y * h / nh);
                out.setColorArgb(x, y, src.getColorArgb(sx, sy));
            }
        }
        return out;
    }

    /** 移除某图片缓存并销毁纹理；无则忽略。 */
    public void release(Path file) {
        Loaded old = cache.remove(file);
        if (old != null) {
            textures.destroyTexture(old.id());
        }
    }

    /** 清空全部缓存（目录重建/配置热重载时调用，O1）。 */
    public void clear() {
        cache.keySet().forEach(this::release);
    }

    /** 当前缓存图片数量（测试/调试用）。 */
    public int size() {
        return cache.size();
    }
}
