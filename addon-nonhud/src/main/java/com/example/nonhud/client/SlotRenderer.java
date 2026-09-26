package com.example.nonhud.client;

import com.example.nonhud.render.SlotRenderState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;

/**
 * 单槽绘制器：把 SlotRenderState 的计算结果画到 HUD。
 * 走 DrawContext.drawTexture（GUI_TEXTURED 管线，支持 alpha 淡入淡出）。
 */
public final class SlotRenderer {

    private SlotRenderer() { }

    /**
     * 绘制一帧。
     * @param context HUD 绘制上下文
     * @param state   该槽渲染状态（已 advance 过）
     * @param texId   当前帧纹理 Identifier
     * @param imgW    图片原始宽（缩放用）
     * @param imgH    图片原始高
     */
    public static void draw(DrawContext context, SlotRenderState state,
                            net.minecraft.util.Identifier texId, int imgW, int imgH) {
        SlotRenderState.Draw d = state.draw(imgW, imgH);
        if (!d.visible() || d.width() <= 0 || d.height() <= 0) {
            return;
        }
        int a = Math.round(d.alpha() * 255.0f);
        int argb = (a << 24) | 0xFFFFFF;
        // 13 参重载：width/height=目标绘制尺寸，regionWidth/regionHeight=源采样区域（整图），
        // textureWidth/Height=纹理原始尺寸。旧 11 参版本会把 region 当 d.width×d.height，
        // 导致只采样纹理左上角一小块再拉伸（图片显示不完全）。
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texId,
                d.x(), d.y(), 0f, 0f,
                d.width(), d.height(), imgW, imgH, imgW, imgH, argb);
    }
}
