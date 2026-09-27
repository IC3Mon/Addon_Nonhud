package com.example.nonhud.config;

import com.example.nonhud.NoNHudAddon;
import com.example.nonhud.NoNHudRuntime;
import com.example.nonhud.render.ScaleMode;
import com.example.nonhud.scan.Slot;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.EnumSet;

/**
 * 模块 5 设置界面（设计文档 §4.4：快捷键呼出、游戏内配置）。
 *
 * 可编辑项（v0.1 精简版）：
 * - 每槽开关（O4 布尔切换）；
 * - 全局 fps / 淡入淡出时长 / 缩放方式（STRETCH vs FIT）。
 * 保存后写入 config.json 并热重载（O3：立即应用 + 重建索引）。
 * 区域/移动方式/Z 序等高级项由 config.json 直接编辑（设置界面后续模块迭代扩展）。
 */
public class ConfigScreen extends Screen {

    private final ConfigManager manager;
    private final Config draft;
    private final Screen parent;
    private final EnumSet<Slot> enabled;
    private TextFieldWidget fpsField;
    private TextFieldWidget fadeInField;
    private TextFieldWidget fadeOutField;
    private TextFieldWidget scaleField;
    private TextFieldWidget numericDurationField;
    private ScaleMode scaleMode;
    private ButtonWidget saveButton;
    /** 动态行距（随窗口高度收缩，16 行布局：8 槽位 + 6 输入 + 2 按钮行）。 */
    private int rowH;
    /** 控件高度 = min(20, rowH)，防止行距缩小时重叠。 */
    private int wh;
    /** 输入区起始 Y（第 8 行起）。 */
    private int fieldsBase;

    public ConfigScreen(ConfigManager manager, Screen parent) {
        super(Text.literal("NoNHud Config"));
        this.manager = manager;
        this.parent = parent;
        this.draft = manager.current().copy();
        this.enabled = EnumSet.copyOf(draft.slotsEnabled);
        this.scaleMode = draft.scaleMode;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int topY = 40;
        int totalRows = 16;
        // 窗口自适应：行距随窗口高度收缩（g8 验收：设置界面按游戏窗口大小自适应）
        this.rowH = Math.max(16, Math.min(22, (this.height - topY - 16) / totalRows));
        this.wh = Math.min(20, this.rowH);
        this.fieldsBase = topY + 8 * this.rowH;

        int row = 0;
        for (Slot s : Slot.values()) {
            final Slot slot = s;
            int yy = topY + row * this.rowH;
            addDrawableChild(ButtonWidget.builder(
                            Text.literal(slotLabel(slot) + ": " + (enabled.contains(slot) ? "ON" : "OFF")),
                            b -> toggle(slot, b))
                    .dimensions(cx - 140, yy, 200, this.wh)
                    .build());
            row++;
        }

        fpsField = new TextFieldWidget(this.textRenderer, cx - 140, fieldsBase, 200, this.wh, Text.literal("fps"));
        fpsField.setText(String.valueOf(draft.fps));
        fpsField.setPlaceholder(Text.literal("fps (default 2)"));
        addDrawableChild(fpsField);

        fadeInField = new TextFieldWidget(this.textRenderer, cx - 140, fieldsBase + this.rowH, 200, this.wh, Text.literal("fadeInMs"));
        fadeInField.setText(String.valueOf(draft.fadeInMs));
        addDrawableChild(fadeInField);

        fadeOutField = new TextFieldWidget(this.textRenderer, cx - 140, fieldsBase + 2 * this.rowH, 200, this.wh, Text.literal("fadeOutMs"));
        fadeOutField.setText(String.valueOf(draft.fadeOutMs));
        addDrawableChild(fadeOutField);

        addDrawableChild(CyclingButtonWidget.<ScaleMode>builder(
                        (mode) -> Text.literal("scaleMode: " + mode), () -> scaleMode)
                .values(ScaleMode.FIT, ScaleMode.STRETCH)
                .build(cx - 140, fieldsBase + 3 * this.rowH, 200, this.wh, Text.literal("scaleMode: "), (b, v) -> scaleMode = v));

        scaleField = new TextFieldWidget(this.textRenderer, cx - 140, fieldsBase + 4 * this.rowH, 200, this.wh, Text.literal("scalePercent"));
        scaleField.setText(String.valueOf(Math.round(draft.scalePercent * 100.0)));
        scaleField.setPlaceholder(Text.literal("图片大小 % (10-500, 100=正常)"));
        addDrawableChild(scaleField);

        numericDurationField = new TextFieldWidget(this.textRenderer, cx - 140, fieldsBase + 5 * this.rowH, 200, this.wh, Text.literal("numericDurationMs"));
        numericDurationField.setText(String.valueOf(draft.numericDurationMs));
        numericDurationField.setPlaceholder(Text.literal("numeric 播放时长 ms (默认3000)"));
        addDrawableChild(numericDurationField);

        saveButton = ButtonWidget.builder(Text.literal("Save & Apply"), b -> saveAndClose())
                .dimensions(cx - 140, fieldsBase + 6 * this.rowH, 200, this.wh)
                .build();
        addDrawableChild(saveButton);

        // 最后一行：Edit Areas 与「打开资源文件夹」两个半宽按钮并排（g8 验收）
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Edit Areas"),
                        b -> this.client.setScreen(new com.example.nonhud.client.AreaEditScreen(manager)))
                .dimensions(cx - 140, fieldsBase + 7 * this.rowH, 96, this.wh)
                .build());

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("打开资源文件夹"),
                        b -> openResourceFolder())
                .dimensions(cx - 36, fieldsBase + 7 * this.rowH, 96, this.wh)
                .build());
    }

    /** 通过系统资源管理器打开模组资源根目录 run/NoNHud（g8 验收）。 */
    private void openResourceFolder() {
        try {
            Util.getOperatingSystem().open(NoNHudRuntime.root());
            NoNHudAddon.LOGGER.info("[NoNHudAddon] opened resource folder: {}", NoNHudRuntime.root());
        } catch (Exception e) {
            NoNHudAddon.LOGGER.error("[NoNHudAddon] failed to open resource folder: {}", NoNHudRuntime.root(), e);
        }
    }

    private void toggle(Slot slot, ButtonWidget button) {
        if (!enabled.remove(slot)) {
            enabled.add(slot);
        }
        button.setMessage(Text.literal(slotLabel(slot) + ": " + (enabled.contains(slot) ? "ON" : "OFF")));
    }

    private void saveAndClose() {
        try {
            draft.fps = Double.parseDouble(fpsField.getText());
        } catch (NumberFormatException e) {
            // 非法输入：保留原值
        }
        try {
            draft.fadeInMs = Long.parseLong(fadeInField.getText());
        } catch (NumberFormatException e) {
            // 保留原值
        }
        try {
            draft.fadeOutMs = Long.parseLong(fadeOutField.getText());
        } catch (NumberFormatException e) {
            // 保留原值
        }
        try {
            double percent = Double.parseDouble(scaleField.getText());
            draft.scalePercent = Math.max(0.1, Math.min(5.0, percent / 100.0));
        } catch (NumberFormatException e) {
            // 保留原值
        }
        try {
            draft.numericDurationMs = Math.max(0, Long.parseLong(numericDurationField.getText()));
        } catch (NumberFormatException e) {
            // 保留原值
        }
        draft.scaleMode = scaleMode;
        draft.slotsEnabled = enabled;
        manager.updateAndApply(draft);
        // 配置热重载后重建渲染实例，使新 area/分组立即生效（g8 验收：area 修改后绘制位置更新）
        com.example.nonhud.client.HudRenderer.INSTANCE.invalidateStates();
        NoNHudAddon.LOGGER.info("[NoNHudAddon] config saved: fps={}, scaleMode={}, scalePercent={}, numericDurationMs={}, slotsEnabled={}",
                draft.fps, draft.scaleMode, draft.scalePercent, draft.numericDurationMs, enabled);
        this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1.21.2+ 的 renderBackground(3参) 内部触发 applyBlur（每帧限一次），
        // 与 fabric-screen-api-v1 的渲染桥接冲突（Can only blur once per frame），
        // 故改用纯色半透明背景，不启用背景模糊。
        context.fill(0, 0, this.width, this.height, 0xA0000000);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("NoNHud Settings"),
                this.width / 2, 16, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);

        // 每个输入项右侧的提示信息（g6：数值框旁给出含义与范围；g8：随动态行距定位）
        int base = this.fieldsBase;
        int hx = this.width / 2 + 70;
        int color = 0xFFCCCCCC;
        int off = Math.max(0, (this.wh - 8) / 2);
        context.drawTextWithShadow(this.textRenderer, Text.literal("每秒帧数 (>=0.1, 默认2)"), hx, base + off, color);
        context.drawTextWithShadow(this.textRenderer, Text.literal("淡入时长 ms (默认300)"), hx, base + this.rowH + off, color);
        context.drawTextWithShadow(this.textRenderer, Text.literal("淡出时长 ms (默认300)"), hx, base + 2 * this.rowH + off, color);
        context.drawTextWithShadow(this.textRenderer, Text.literal("FIT=等比完整显示 / STRETCH=拉伸填满"), hx, base + 3 * this.rowH + off, color);
        context.drawTextWithShadow(this.textRenderer, Text.literal("图片大小 10-500% (100=正常)"), hx, base + 4 * this.rowH + off, color);
        context.drawTextWithShadow(this.textRenderer, Text.literal("数值槽播放时长 ms, 0=一直显示 (默认3000)"), hx, base + 5 * this.rowH + off, color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static String slotLabel(Slot slot) {
        return switch (slot) {
            case A -> "A (event)";
            case V -> "V (event)";
            case M -> "M (event)";
            case BOTLE -> "botle";
            case VFULL -> "Vfull (numeric)";
            case AFULL -> "Afull (numeric)";
            case PREGNANT -> "pregnant";
            case FXXKME -> "fxxkme (lust)";
        };
    }
}
