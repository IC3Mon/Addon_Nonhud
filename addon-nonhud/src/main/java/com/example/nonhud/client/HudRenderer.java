package com.example.nonhud.client;

import com.example.nonhud.ChatDebug;
import com.example.nonhud.NoNHudRuntime;
import com.example.nonhud.render.Group;
import com.example.nonhud.render.OverlapPlacer;
import com.example.nonhud.render.RenderDefaults;
import com.example.nonhud.render.RenderKey;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 模块 4 渲染器（设计文档 §5 + N1/N2/O2；模块 5.1 分组重构）：
 * - 注册 HudRenderCallback，每渲染帧消费 SlotDisplayRegistry 快照；
 * - 槽可属于多个组（多组多份显示）：以 RenderKey(槽, 组) 为渲染实例；
 * - 每组的 area 为图片生成位置 + 边界约束（SlotRenderState 内约束），组 fps/淡入/淡出独立；
 * - 组内多槽按 Z 序绘制，开启重叠时用 OverlapPlacer 错开防止完全重叠；
 * - 组间绘制层级：BACK 组先绘（下层），FRONT 组后绘（上层）；
 * - N1 全局暂停门控：暂停态仍推进状态机（淡入淡出/帧推进），仅不绘制；
 * - O2：按当前帧路径经 TextureCache 装载/释放纹理；
 * - 槽消失 → 淡出 → 移除。
 */
public final class HudRenderer {

    public static final HudRenderer INSTANCE = new HudRenderer();

    /** 虚拟画布尺寸（缩放像素，与 AreaEditScreen 及 HUD 配置坐标系一致）。 */
    public static final int CANVAS_W = 640;
    public static final int CANVAS_H = 360;

    /** 懒加载：首次渲染时才创建，确保此时客户端完全就绪（渲染线程上 TextureManager 非 null）。 */
    private TextureCache textures;
    private final Map<RenderKey, SlotRenderState> states = new LinkedHashMap<>();
    private final Map<RenderKey, SlotKey> lastKey = new HashMap<>();
    /** 已提示过装载失败的文件路径（避免每帧刷屏）。 */
    private final Set<java.nio.file.Path> failedLoadLogged = new HashSet<>();
    /** 本次激活已输出过"已绘制"的槽（重激活时重置，避免每帧刷屏）。 */
    private final Set<RenderKey> drawLogged = new HashSet<>();
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

    /** 测试/调试：当前活跃渲染实例数（槽×组）。 */
    public int activeCount() {
        return states.size();
    }

    /** 每帧：推进状态机 → 按组层级/Z 序绘制（N1 门控）。 */
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

        // 1) 激活/切换：快照中每槽 → 其所属各组各建/切一个实例（槽可多组多份显示）
        for (Map.Entry<Slot, SlotKey> e : snapshot.entrySet()) {
            Slot slot = e.getKey();
            SlotKey key = e.getValue();
            for (Group g : RenderDefaults.groupsOf(slot)) {
                RenderKey rk = new RenderKey(slot, g.id());
                SlotRenderState state = states.get(rk);
                if (state == null) {
                    // 新激活：查图集候选，无候选则该槽静默不播
                    FrameSequence seq = pickSequence(key);
                    if (seq == null) {
                        ChatDebug.client("播放图片: 槽位=" + slot + " 组=" + g.id() + " 档位=" + key.phaseKey() + " 无图集候选 → 不播");
                        lastKey.remove(rk);
                        continue;
                    }
                    state = new SlotRenderState(slot, key, seq,
                            RenderDefaults.region(slot), RenderDefaults.moveMode(slot),
                            RenderDefaults.scaleMode(), RenderDefaults.scalePercent(),
                            g, g.fadeInMs(), g.fadeOutMs(),
                            new Random());
                    states.put(rk, state);
                    lastKey.put(rk, key);
                    ChatDebug.client("播放图片: 槽位=" + slot + " 组=" + g.id() + " 档位=" + key.phaseKey()
                            + " 图片=" + seq.current());
                } else if (!state.key().equals(key)) {
                    // 档位切换：释放旧档纹理、重选序列并重新淡入
                    releaseTexturesFor(state);
                    FrameSequence seq = pickSequence(key);
                    lastKey.put(rk, key);
                    if (seq == null) {
                        ChatDebug.client("播放图片: 槽位=" + slot + " 组=" + g.id() + " 切档 " + key.phaseKey() + " 无图集候选 → 停播");
                        state.deactivate();
                        continue;
                    }
                    SlotRenderState next = new SlotRenderState(slot, key, seq,
                            RenderDefaults.region(slot), RenderDefaults.moveMode(slot),
                            RenderDefaults.scaleMode(), RenderDefaults.scalePercent(),
                            g, g.fadeInMs(), g.fadeOutMs(),
                            new Random());
                    states.put(rk, next);
                    ChatDebug.client("播放图片: 槽位=" + slot + " 组=" + g.id() + " 切档 " + key.phaseKey()
                            + " 图片=" + seq.current());
                }
            }
        }
        // 2) 快照中消失的槽 → 该槽所有组实例淡出
        for (RenderKey rk : List.copyOf(states.keySet())) {
            if (!snapshot.containsKey(rk.slot())) {
                states.get(rk).deactivate();
            }
        }

        // 3) 推进所有实例
        for (SlotRenderState s : states.values()) {
            s.advance(delta);
        }

        // 4) 绘制（N1 门控；组 BACK→FRONT，组内按 Z 序，组内开启重叠时错开）
        if (!paused) {
            // 虚拟画布 640×360 → 实际缩放屏映射（窗口大小调整后区域/图片自适应，不铺满）
            MinecraftClient mc = MinecraftClient.getInstance();
            double scaleX = mc.getWindow().getScaledWidth() > 0
                    ? mc.getWindow().getScaledWidth() / (double) CANVAS_W : 1.0;
            double scaleY = mc.getWindow().getScaledHeight() > 0
                    ? mc.getWindow().getScaledHeight() / (double) CANVAS_H : 1.0;
            List<Group> ordered = new ArrayList<>(RenderDefaults.groups());
            ordered.sort(Comparator.comparingInt(g -> g.layer() == Group.Layer.FRONT ? 1 : 0));
            List<Slot> zOrder = ZOrder.sorted(RenderDefaults.zOrder());
            for (Group g : ordered) {
                List<SlotRenderState.Draw> placed = new ArrayList<>();
                for (Slot slot : zOrder) {
                    RenderKey rk = new RenderKey(slot, g.id());
                    SlotRenderState s = states.get(rk);
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
                    SlotRenderState.Draw d = s.draw(loaded.width(), loaded.height());
                    d = OverlapPlacer.place(g, d, placed, new Random());
                    placed.add(d);
                    if (drawLogged.add(rk)) {
                        ChatDebug.client("已绘制: 槽位=" + slot + " 组=" + g.id()
                                + " 图片=" + s.currentFramePath()
                                + " 纹理=" + loaded.width() + "x" + loaded.height());
                    }
                    // 组 area 约束在虚拟坐标中完成，最终绘制前统一映射到实际缩放屏
                    SlotRenderer.draw(context, loaded.id(), scaleDraw(d, scaleX, scaleY),
                            loaded.width(), loaded.height());
                }
            }
        }

        // 5) 清理：淡出完成的实例移除并释放纹理（O2）
        for (RenderKey rk : List.copyOf(states.keySet())) {
            SlotRenderState s = states.get(rk);
            if (!s.active() && s.alpha() <= 0f) {
                releaseTexturesFor(s);
                states.remove(rk);
                drawLogged.remove(rk);
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

    /** 把虚拟画布坐标的绘制矩形映射到实际缩放屏（窗口自适应）。 */
    private static SlotRenderState.Draw scaleDraw(SlotRenderState.Draw d, double scaleX, double scaleY) {
        if (scaleX == 1.0 && scaleY == 1.0) {
            return d;
        }
        return new SlotRenderState.Draw(
                (int) Math.round(d.x() * scaleX),
                (int) Math.round(d.y() * scaleY),
                (int) Math.max(1, Math.round(d.width() * scaleX)),
                (int) Math.max(1, Math.round(d.height() * scaleY)),
                d.alpha(), d.framePath(), d.visible());
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

    /**
     * 配置热重载（组 area / 渲染参数变化）后重建全部渲染实例：
     * 已激活的 SlotRenderState 持有创建时的 Group 引用（含旧 area），
     * 清空后下一帧按新配置重建，使编辑后的区域立即生效。
     */
    public void invalidateStates() {
        states.clear();
        lastKey.clear();
        drawLogged.clear();
    }
}
