package com.example.nonhud.client;

import net.minecraft.client.texture.NativeImage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TextureCache 缩略逻辑（O2 大图装载）：超大原图先压到 MAX_TEXTURE_EDGE 内再上传，
 * 避免超出 GPU 纹理上限（2499x4148 > 4096）导致"播放了图片但屏幕不显示"。
 * 纯像素操作，无需 GL 环境。
 */
class TextureCacheTest {

    @Test
    void smallImageReturnedAsIs() {
        NativeImage img = new NativeImage(64, 32, true);
        NativeImage out = TextureCache.clampToMaxEdge(img, TextureCache.MAX_TEXTURE_EDGE);
        assertSame(img, out);
        img.close();
    }

    @Test
    void boundaryExactlyAtMaxEdgeStaysUnchanged() {
        NativeImage img = new NativeImage(1024, 500, true);
        NativeImage out = TextureCache.clampToMaxEdge(img, 1024);
        assertSame(img, out);
        img.close();
    }

    @Test
    void largeImageDownscaledWithinMaxEdge() {
        NativeImage img = new NativeImage(2499, 4148, true);
        NativeImage out = TextureCache.clampToMaxEdge(img, 1024);
        int m = Math.max(out.getWidth(), out.getHeight());
        assertTrue(m <= 1024, "max edge after downscale = " + m);
        // 宽高比保持（±1px 取整误差）
        double srcRatio = 2499.0 / 4148.0;
        double outRatio = (double) out.getWidth() / out.getHeight();
        assertEquals(srcRatio, outRatio, 0.01);
        img.close();
        out.close();
    }

    @Test
    void downscaledPixelPreservesSourceSample() {
        NativeImage img = new NativeImage(100, 100, true);
        img.setColorArgb(50, 50, 0x11223344);
        NativeImage out = TextureCache.clampToMaxEdge(img, 50);
        // 最近邻映射：目标 (25,25) ← 源 (min(99, 25*100/50)) = (50,50)
        assertEquals(0x11223344, out.getColorArgb(25, 25));
        img.close();
        out.close();
    }

    /** 真实图集文件（超大 PNG）能被 NativeImage 解码并缩略——区分解码失败与 GL 上传失败。 */
    @Test
    void realAtlasFileDecodesAndClamps() throws IOException {
        Path p = Paths.get("run", "NoNHud", "numeric", "Vfull", "up", "Vfullup1-ph1-f01.png");
        if (!Files.isRegularFile(p)) {
            return; // 不强制依赖外部资源
        }
        try (InputStream in = Files.newInputStream(p)) {
            NativeImage img = NativeImage.read(in);
            assertTrue(img.getWidth() > 0 && img.getHeight() > 0, "解码后尺寸应有效");
            NativeImage out = TextureCache.clampToMaxEdge(img, TextureCache.MAX_TEXTURE_EDGE);
            assertTrue(Math.max(out.getWidth(), out.getHeight()) <= TextureCache.MAX_TEXTURE_EDGE,
                    "缩略后最大边应受限");
            img.close();
            out.close();
        }
    }
}
