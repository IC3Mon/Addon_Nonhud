package com.example.nonhud.config;

import com.example.nonhud.client.HudRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

/**
 * 模块 5 快捷键：
 * - 暂停键（N1）：切换 HUD 全局暂停（仅隐藏绘制，状态机继续推进）；
 * - 设置键：打开设置界面（ConfigScreen，内含 "Edit Areas" 入口打开区域编辑）。
 * 默认键位可经 config.json "keys" 覆盖（"key.keyboard.p" / "key.keyboard.f6" 等）。
 */
public final class ConfigKeybinds {

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(net.minecraft.util.Identifier.of("nonhud", "key_categories"));

    private static KeyBinding pauseBinding;
    private static KeyBinding settingsBinding;

    private ConfigKeybinds() { }

    /** 注册键位并挂按键事件（客户端初始化调用）。 */
    public static void register() {
        Config cfg = ConfigManagers.INSTANCE.current();
        pauseBinding = new KeyBinding("key.nonhud.pause",
                code(cfg.pauseKey, "key.keyboard.p"), CATEGORY);
        settingsBinding = new KeyBinding("key.nonhud.settings",
                code(cfg.settingsKey, "key.keyboard.f6"), CATEGORY);
        KeyBindingHelper.registerKeyBinding(pauseBinding);
        KeyBindingHelper.registerKeyBinding(settingsBinding);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pauseBinding.wasPressed()) {
                boolean paused = !HudRenderer.INSTANCE.isPaused();
                HudRenderer.INSTANCE.setPaused(paused);
                client.inGameHud.setOverlayMessage(
                        Text.literal("[NoNHud] HUD " + (paused ? "paused" : "resumed")), false);
            }
            if (settingsBinding.wasPressed()) {
                client.setScreen(new ConfigScreen(ConfigManagers.INSTANCE, client.currentScreen));
            }
        });
    }

    /** 配置键名（"key.keyboard.p"）→ GLFW 键码；空/非法回退默认键名。 */
    private static int code(String keyName, String fallbackKey) {
        String name = (keyName == null || keyName.isBlank()) ? fallbackKey : keyName;
        try {
            return net.minecraft.client.util.InputUtil.fromTranslationKey(name).getCode();
        } catch (RuntimeException e) {
            return net.minecraft.client.util.InputUtil.fromTranslationKey(fallbackKey).getCode();
        }
    }
}
