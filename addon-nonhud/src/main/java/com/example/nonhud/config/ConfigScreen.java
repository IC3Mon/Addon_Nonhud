package com.example.nonhud.config;

import com.example.nonhud.NoNHudAddon;
import com.example.nonhud.render.ScaleMode;
import com.example.nonhud.scan.Slot;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

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
        int y = 40;
        int row = 0;
        for (Slot s : Slot.values()) {
            final Slot slot = s;
            int yy = y + row * 22;
            addDrawableChild(ButtonWidget.builder(
                            Text.literal(slotLabel(slot) + ": " + (enabled.contains(slot) ? "ON" : "OFF")),
                            b -> toggle(slot, b))
                    .dimensions(cx - 140, yy, 200, 20)
                    .build());
            row++;
        }

        int base = y + row * 22 + 8;
        fpsField = new TextFieldWidget(this.textRenderer, cx - 140, base, 200, 20, Text.literal("fps"));
        fpsField.setText(String.valueOf(draft.fps));
        fpsField.setPlaceholder(Text.literal("fps (default 2)"));
        addDrawableChild(fpsField);

        fadeInField = new TextFieldWidget(this.textRenderer, cx - 140, base + 24, 200, 20, Text.literal("fadeInMs"));
        fadeInField.setText(String.valueOf(draft.fadeInMs));
        addDrawableChild(fadeInField);

        fadeOutField = new TextFieldWidget(this.textRenderer, cx - 140, base + 48, 200, 20, Text.literal("fadeOutMs"));
        fadeOutField.setText(String.valueOf(draft.fadeOutMs));
        addDrawableChild(fadeOutField);

        addDrawableChild(CyclingButtonWidget.<ScaleMode>builder(
                        (mode) -> Text.literal("scaleMode: " + mode), () -> scaleMode)
                .values(ScaleMode.FIT, ScaleMode.STRETCH)
                .build(cx - 140, base + 72, 200, 20, Text.literal("scaleMode: "), (b, v) -> scaleMode = v));

        scaleField = new TextFieldWidget(this.textRenderer, cx - 140, base + 96, 200, 20, Text.literal("scalePercent"));
        scaleField.setText(String.valueOf(Math.round(draft.scalePercent * 100.0)));
        scaleField.setPlaceholder(Text.literal("图片大小 % (10-500, 100=正常)"));
        addDrawableChild(scaleField);

        numericDurationField = new TextFieldWidget(this.textRenderer, cx - 140, base + 120, 200, 20, Text.literal("numericDurationMs"));
        numericDurationField.setText(String.valueOf(draft.numericDurationMs));
        numericDurationField.setPlaceholder(Text.literal("numeric 播放时长 ms (默认3000)"));
        addDrawableChild(numericDurationField);

        saveButton = ButtonWidget.builder(Text.literal("Save & Apply"), b -> saveAndClose())
                .dimensions(cx - 140, base + 144, 200, 20)
                .build();
        addDrawableChild(saveButton);
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
