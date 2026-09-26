package com.example.nonhud.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * 负责在 HUD 层绘制一张全屏 / 居中图片。
 * 完全自绘，不依赖 NoN 客户端渲染 API，因此必然可编译运行。
 */
public final class HudImageRenderer {
    public static final HudImageRenderer INSTANCE = new HudImageRenderer();

    /** 当前要显示图的 (key)，key = "animationId|stage"，null 表示不显示。 */
    private volatile String currentImageKey;

    private HudImageRenderer() { }

    public void register() {
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(
            (DrawContext context, RenderTickCounter tickCounter) -> {
                String key = this.currentImageKey;
                if (key == null) return;
                Identifier tex = ImageCatalog.imageFor(key);
                if (tex == null) return;
                drawFullscreen(context, tex);
            });
    }

    /** 设置当前要显示的图。key 为 null 则隐藏。 */
    public void setCurrent(String animationId, int stage) {
        this.currentImageKey = (animationId == null) ? null : animationId + "|" + stage;
    }

    private void drawFullscreen(DrawContext context, Identifier texture) {
        MinecraftClient mc = MinecraftClient.getInstance();
        // 以屏幕中心为基准绘制，达到图像中心对齐效果
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int size = Math.min(sw, sh);
        int x = (sw - size) / 2;
        int y = (sh - size) / 2;
        context.drawTexturedQuad(texture, x, y, size, size, 0f, 0f, 1f, 1f);
    }
}