package com.example.nonhud.client;

import com.example.nonhud.ChatDebug;
import com.example.nonhud.NoNHudAddon;
import com.example.nonhud.NoNHudRuntime;
import com.example.nonhud.render.RenderDefaults;
import com.example.nonhud.render.SlotRenderState;
import com.example.nonhud.render.ZOrder;
import com.example.nonhud.scan.FrameSequence;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotIndex;
import com.example.nonhud.scan.SlotKey;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 模块 4 渲染器（设计文档 §5 + N1/N2/O2）：
 * - 注册 HudRenderCallback，每渲染帧消费 SlotDisplayRegistry 快照；
 * - 按 Z 序（N2）先绘低序再绘高序；
 * - N1 全局暂停门控：暂停态仍推进状态机（淡入淡出/帧推进），仅不绘制；
 * - O2：按当前帧路径经 TextureCache 装载/释放纹理；
 * - 槽消失 → 淡出 → 移除。
 */
public final class HudRenderer {

    public static final HudRenderer INSTANCE = new HudRenderer();

    /** 懒加载：首次渲染时才创建，确保此时客户端完全就绪（渲染线程上 TextureManager 非 null）。 */
    private TextureCache textures;
    private final Map<Slot, SlotRenderState> states = new EnumMap<>(Slot.class);
    private final Map<Slot, SlotKey> lastKey = new EnumMap<>(Slot.class);
    /** 已提示过装载失败的文件路径（避免每帧刷屏）。 */
    private final Set<java.nio.file.Path> failedLoadLogged = new HashSet<>();
    /** 本次激活已输出过"已绘制"的槽（重激活时重置，避免每帧刷屏）。 */
    private final Set<Slot> drawLogged = new HashSet<>();
    private volatile boolean paused;

    private long lastFrameMs = -1;

    private HudRenderer() { }

    /** 首次使用纹理缓存时创建（渲染线程上客户端已完全就绪，TextureManager 非 null）。 */
    private TextureCache textures() {
        if (textures == null) {
            textures = new TextureCache(MinecraftClient.getInstance().getTextureManager());
        }
        return textures;
    }

    /** 注册 HUD 渲染回调（客户端初始化调用）。 */
    public void register() {
        HudRenderCallback.EVENT.register(this::render);
    }

    /** N1：全局暂停/恢复（仅隐藏绘制，不影响状态机推进）。 */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    /** 测试/调试：当前活跃渲染实例数。 */
    public int activeCount() {
        return states.size();
    }

    /** 每帧：推进状态机 → 按 Z 序绘制（N1 门控）。 */
    private void render(DrawContext context, RenderTickCounter tickCounter) {
        if (lastFrameMs < 0) {
            lastFrameMs = currentTimeMs();
            return;
        }
        long now = currentTimeMs();
        long delta = Math.min(now - lastFrameMs, 1000L);
        lastFrameMs = now;
        if (delta <= 0) {
            return;
        }

        Map<Slot, SlotKey> snapshot = SlotDisplayRegistry.INSTANCE.snapshot();

        // 1) 激活/切换：按快照更新各槽状态
        for (Map.Entry<Slot, SlotKey> e : snapshot.entrySet()) {
            Slot slot = e.getKey();
            SlotKey key = e.getValue();
            SlotRenderState state = states.get(slot);
            if (state == null) {
                // 新激活：查图集候选，无候选则该槽静默不播
                FrameSequence seq = pickSequence(key);
                if (seq == null) {
                    ChatDebug.client("播放图片: 槽位=" + slot + " 档位=" + key.phaseKey() + " 无图集候选 → 不播");
                    lastKey.remove(slot);
                    continue;
                }
                state = new SlotRenderState(slot, key, seq,
                        RenderDefaults.region(slot), RenderDefaults.moveMode(slot),
                        RenderDefaults.scaleMode(), RenderDefaults.scalePercent(),
                        RenderDefaults.fadeInMs(), RenderDefaults.fadeOutMs(),
                        new Random());
                states.put(slot, state);
                lastKey.put(slot, key);
                ChatDebug.client("播放图片: 槽位=" + slot + " 档位=" + key.phaseKey()
                        + " 图片=" + seq.current());
            } else if (!state.key().equals(key)) {
                // 档位切换：释放旧档纹理、重选序列并重新淡入
                releaseTexturesFor(state);
                FrameSequence seq = pickSequence(key);
                lastKey.put(slot, key);
                if (seq == null) {
                    ChatDebug.client("播放图片: 槽位=" + slot + " 切档 " + key.phaseKey() + " 无图集候选 → 停播");
                    state.deactivate();
                    continue;
                }
                SlotRenderState next = new SlotRenderState(slot, key, seq,
                        RenderDefaults.region(slot), RenderDefaults.moveMode(slot),
                        RenderDefaults.scaleMode(), RenderDefaults.scalePercent(),
                        RenderDefaults.fadeInMs(), RenderDefaults.fadeOutMs(),
                        new Random());
                states.put(slot, next);
                ChatDebug.client("播放图片: 槽位=" + slot + " 切档 " + key.phaseKey()
                        + " 图片=" + seq.current());
            }
        }
        // 2) 快照中消失的槽 → 淡出
        for (Slot slot : List.copyOf(states.keySet())) {
            if (!snapshot.containsKey(slot)) {
                states.get(slot).deactivate();
            }
        }

        // 3) 推进所有实例
        for (SlotRenderState s : states.values()) {
            s.advance(delta);
        }

        // 4) 绘制（N1 门控；按 Z 序）
        if (!paused) {
            List<Slot> order = ZOrder.sorted(RenderDefaults.zOrder());
            for (Slot slot : order) {
                SlotRenderState s = states.get(slot);
                if (s == null || !s.active()) {
                    continue;
                }
                TextureCache.Loaded loaded = textures().load(s.currentFramePath());
                if (loaded == null) {
                    if (failedLoadLogged.add(s.currentFramePath())) {
                        ChatDebug.client("纹理装载失败: " + s.currentFramePath());
                    }
                    continue;
                }
                if (drawLogged.add(slot)) {
                    ChatDebug.client("已绘制: 槽位=" + slot + " 图片=" + s.currentFramePath()
                            + " 纹理=" + loaded.width() + "x" + loaded.height());
                }
                SlotRenderer.draw(context, s, loaded.id(), loaded.width(), loaded.height());
            }
        }

        // 5) 清理：淡出完成的实例移除并释放纹理（O2）
        for (Slot slot : List.copyOf(states.keySet())) {
            SlotRenderState s = states.get(slot);
            if (!s.active() && s.alpha() <= 0f) {
                releaseTexturesFor(s);
                states.remove(slot);
                drawLogged.remove(slot);
            }
        }
        lastKey.keySet().retainAll(states.keySet());
    }

    /** 释放该渲染实例序列用到的全部纹理（档位切换/实例移除时调用，O2）。 */
    private void releaseTexturesFor(SlotRenderState s) {
        for (java.nio.file.Path p : s.sequenceFrames()) {
            textures().release(p);
        }
    }

    /** 从索引按 key 取一个候选序列（随机选前缀）；无候选返回 null。 */
    private FrameSequence pickSequence(SlotKey key) {
        SlotIndex index = NoNHudRuntime.index();
        List<FrameSequence> candidates = index.get(key);
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(new Random().nextInt(candidates.size()));
    }

    private long currentTimeMs() {
        return System.currentTimeMillis();
    }

    /** 释放全部纹理（断线/重载时调用）。 */
    public void releaseAll() {
        if (textures != null) {
            textures().clear();
        }
        states.clear();
        lastKey.clear();
        lastFrameMs = -1;
    }
}
