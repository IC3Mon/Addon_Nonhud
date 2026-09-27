package com.example.nonhud.client;

import com.example.nonhud.config.Config;
import com.example.nonhud.config.ConfigManager;
import com.example.nonhud.render.Group;
import com.example.nonhud.render.Region;
import com.example.nonhud.render.RenderDefaults;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块 5.1：游戏内显示区域（group area）可视化编辑。
 *
 * 画布为缩放屏 640×360（与 HUD 同坐标系，参考 2560x1440 + guiScale4）。
 * 操作（界面顶部有提示）：
 * - 左键：点击组区域 = 选中；在选中组上按住拖动 = 框选新区域（松开生效，最小 10x10）；
 * - 中键：在组区域上按住拖动 = 整体移动该区域；
 * - Save & Close / ESC：把编辑结果写回 config.json 并热重载（RenderDefaults.setGroups）。
 */
public class AreaEditScreen extends Screen {

    /** 画布尺寸（缩放像素，与 HUD 坐标系一致）。 */
    public static final int CANVAS_W = 640;
    public static final int CANVAS_H = 360;

    private static final int MIN_AREA = 10;

    private final ConfigManager manager;
    private final List<Group> groups = new ArrayList<>();
    private Group selected;
    private boolean dragging;
    private int dragStartX;
    private int dragStartY;
    private Region previewRect;
    private boolean moving;
    private int moveStartX;
    private int moveStartY;
    private Region moveStartArea;

    public AreaEditScreen(ConfigManager manager) {
        super(Text.literal("NoNHud Area Edit"));
        this.manager = manager;
        groups.addAll(RenderDefaults.groups());
        if (!groups.isEmpty()) {
            selected = groups.get(0);
        }
    }

    // ---------- 坐标换算：缩放屏 640x360 ↔ 当前屏幕 ----------

    private double scaleX() {
        return this.width / (double) CANVAS_W;
    }

    private double scaleY() {
        return this.height / (double) CANVAS_H;
    }

    private int toScreenX(int canvasX) {
        return (int) Math.round(canvasX * scaleX());
    }

    private int toScreenY(int canvasY) {
        return (int) Math.round(canvasY * scaleY());
    }

    private int toCanvasX(double screenX) {
        return (int) Math.round(screenX / scaleX());
    }

    private int toCanvasY(double screenY) {
        return (int) Math.round(screenY / scaleY());
    }

    // ---------- 命中与编辑 ----------

    /** 命中检查：按列表顺序（后建的组在上），取覆盖鼠标点的最上层组。 */
    private Group hitGroup(int canvasX, int canvasY) {
        Group hit = null;
        for (Group g : groups) {
            Region a = g.area();
            if (canvasX >= a.x() && canvasX < a.x() + a.width()
                    && canvasY >= a.y() && canvasY < a.y() + a.height()) {
                hit = g;
            }
        }
        return hit;
    }

    /** 用新实例替换列表中同 id 的组，并同步选中指针。 */
    private void updateSelectedInList(Group next) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).id().equals(next.id())) {
                groups.set(i, next);
                selected = next;
                return;
            }
        }
    }

    private Region clampToCanvas(Region r) {
        int x = Math.max(0, Math.min(r.x(), CANVAS_W - MIN_AREA));
        int y = Math.max(0, Math.min(r.y(), CANVAS_H - MIN_AREA));
        int w = Math.max(MIN_AREA, Math.min(r.width(), CANVAS_W - x));
        int h = Math.max(MIN_AREA, Math.min(r.height(), CANVAS_H - y));
        return new Region(x, y, w, h);
    }

    private void replaceSelectedArea(Region area) {
        if (selected == null) {
            return;
        }
        Group s = selected;
        updateSelectedInList(new Group(s.id(), s.slots(), clampToCanvas(area),
                s.fps(), s.fadeInMs(), s.fadeOutMs(),
                s.overlapEnabled(), s.overlapPercent(), s.layer()));
    }

    // ---------- 鼠标交互 ----------

    @Override
    public boolean mouseClicked(Click click, boolean bool) {
        // 先交给子组件（Save & Close 按钮等）消费，避免点击按钮时误选中/拖动 area
        if (super.mouseClicked(click, bool)) {
            return true;
        }
        int cx = toCanvasX(click.x());
        int cy = toCanvasY(click.y());
        Group hit = hitGroup(cx, cy);
        if (click.button() == 0) {
            // 左键：选中（点击任意处）并开始框选
            if (hit != null) {
                selected = hit;
            }
            dragging = true;
            dragStartX = cx;
            dragStartY = cy;
            previewRect = new Region(cx, cy, 1, 1);
            return true;
        }
        if (click.button() == 2) {
            // 中键：整体移动
            if (hit != null) {
                selected = hit;
                moving = true;
                moveStartX = cx;
                moveStartY = cy;
                moveStartArea = hit.area();
            }
            return true;
        }
        return super.mouseClicked(click, bool);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        int cx = toCanvasX(click.x());
        int cy = toCanvasY(click.y());
        if (click.button() == 0 && dragging) {
            int x1 = Math.min(dragStartX, cx);
            int y1 = Math.min(dragStartY, cy);
            int x2 = Math.max(dragStartX, cx);
            int y2 = Math.max(dragStartY, cy);
            previewRect = new Region(x1, y1, x2 - x1, y2 - y1);
            return true;
        }
        if (click.button() == 2 && moving && selected != null) {
            int dx = cx - moveStartX;
            int dy = cy - moveStartY;
            Region a = moveStartArea;
            updateSelectedInList(new Group(selected.id(), selected.slots(),
                    new Region(a.x() + dx, a.y() + dy, a.width(), a.height()),
                    selected.fps(), selected.fadeInMs(), selected.fadeOutMs(),
                    selected.overlapEnabled(), selected.overlapPercent(), selected.layer()));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && dragging) {
            dragging = false;
            if (selected != null && previewRect != null) {
                replaceSelectedArea(previewRect);
            }
            previewRect = null;
            return true;
        }
        if (click.button() == 2 && moving) {
            moving = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    // ---------- 保存 ----------

    private void save() {
        Config draft = manager.current().copy();
        draft.groups = new ArrayList<>(groups);
        manager.updateAndApply(draft);
        // 重建渲染实例：已激活状态持有旧 Group（旧 area），清空后下一帧按新 area 重建
        com.example.nonhud.client.HudRenderer.INSTANCE.invalidateStates();
    }

    @Override
    public void close() {
        save();
        super.close();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), b -> close())
                .dimensions(cx - 60, 8, 120, 20)
                .build());
    }

    // ---------- 渲染 ----------

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xA0000000);

        // 画布背景（半透明），确认缩放映射范围
        context.fill(toScreenX(0), toScreenY(0), toScreenX(CANVAS_W), toScreenY(CANVAS_H), 0x33000000);

        // 各组的 area 框
        for (Group g : groups) {
            Region a = g.area();
            boolean sel = g.equals(selected);
            int color = sel ? 0xFF00E000 : 0xFFCCCCCC;
            int fill = sel ? 0x2200E000 : 0x11000000;
            int x1 = toScreenX(a.x());
            int y1 = toScreenY(a.y());
            int x2 = toScreenX(a.x() + a.width());
            int y2 = toScreenY(a.y() + a.height());
            context.fill(x1, y1, x2, y2, fill);
            context.fill(x1, y1, x2, y1 + 1, color);
            context.fill(x1, y2 - 1, x2, y2, color);
            context.fill(x1, y1, x1 + 1, y2, color);
            context.fill(x2 - 1, y1, x2, y2, color);
            String label = g.id() + " [" + joinSlots(g) + "]  x=" + a.x() + " y=" + a.y()
                    + " " + a.width() + "x" + a.height();
            context.drawTextWithShadow(this.textRenderer, Text.literal(label),
                    Math.max(2, Math.min(x1 + 3, this.width - 240)), Math.max(2, y1 + 3), color);
        }

        // 框选预览
        if (dragging && previewRect != null) {
            int x1 = toScreenX(previewRect.x());
            int y1 = toScreenY(previewRect.y());
            int x2 = toScreenX(previewRect.x() + previewRect.width());
            int y2 = toScreenY(previewRect.y() + previewRect.height());
            context.fill(x1, y1, x2, y2, 0x44FFFFFF);
            context.fill(x1, y1, x2, y1 + 1, 0xFFFFFFFF);
            context.fill(x1, y2 - 1, x2, y2, 0xFFFFFFFF);
            context.fill(x1, y1, x1 + 1, y2, 0xFFFFFFFF);
            context.fill(x2 - 1, y1, x2, y2, 0xFFFFFFFF);
        }

        // 操作提示
        int hy = 32;
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("左键: 点击选择组 / 在选中组上拖动框选新区域（松开生效，最小 " + MIN_AREA + "x" + MIN_AREA + "）"),
                4, hy, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("中键: 在组区域上拖动 = 整体移动 | ESC / Save & Close 保存并热重载"),
                4, hy + 12, 0xFFFFFF);

        // 按钮等子组件绘制在最上层
        super.render(context, mouseX, mouseY, delta);
    }

    private static String joinSlots(Group g) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < g.slots().size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(g.slots().get(i).name());
        }
        return sb.toString();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
