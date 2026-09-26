package com.example.nonhud.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 模块 0：外部 NoNHud/ 目录扫描器。
 *
 * 识别 event/numeric 两级目录（功能设计文档 §2/§11）：
 * - event/A/{female,male}/{pre,peak,post}、event/V/{pre,peak,post}、event/M/{pre,peak,post}、event/botle/
 * - numeric/Vfull/{up,re}/、numeric/Afull/{up,re}/、numeric/pregnant/(+birth/)、numeric/fxxkme/
 *
 * 解析规则（§2.3）：
 * - 仅认 .jpg/.png，其余文件忽略；
 * - 事件槽文件名 前缀_fYY，阶段由所在子目录表达；
 * - 数值槽文件名 前缀_phX_fYY，phX=数值分档；
 * - 不合规范但为图片的文件（不带 _fYY）作为单张图装载（单帧、不循环）；
 * - 空/缺目录静默跳过（不报错）。
 *
 * 纯 Java 工具层，不依赖 MC 运行时，便于单元测试。
 */
public final class DirectoryScanner {

    /**
     * 事件槽文件名：前缀[-_]fYY（帧序号 1 位以上，兼容 2 位习惯）。
     * 分隔符同时接受 `-` 与 `_`：规范写作 前缀_fYY（§2.2），示例图为 A1-f01.png（§11.2）。
     */
    private static final Pattern EVENT_FRAME =
            Pattern.compile("(.+?)[-_]f(\\d+)\\.(jpg|png)$", Pattern.CASE_INSENSITIVE);
    /** 数值槽文件名：前缀[-_]phN[-_]fYY。 */
    private static final Pattern NUMERIC_FRAME =
            Pattern.compile("(.+?)[-_]ph(\\d+)[-_]f(\\d+)\\.(jpg|png)$", Pattern.CASE_INSENSITIVE);

    /** 扫描器默认 fps（设计文档 §6 fps: 2；模块 4 配置落地后可覆盖）。 */
    public static final double DEFAULT_FPS = 2.0;

    private static final String[] EVENT_TIERS = {"pre", "peak", "post"};
    private static final String[] GENDERS = {"female", "male"};
    private static final String[] DIRECTIONS = {"up", "re"};

    /** 阶段键构造器：numeric 槽使用文件名解析出的 phX，event/单档槽固定为 0。 */
    private interface KeyForStage {
        SlotKey key(int ph);
    }

    private DirectoryScanner() { }

    /**
     * 扫描 NoNHud 根目录，构建槽索引。
     *
     * @param root    NoNHud/ 目录（不存在时返回空索引，不报错）
     * @param enabled 启用的槽位集合（O4 每槽开关）；null 或空 = 全部启用
     */
    public static SlotIndex scan(Path root, Set<Slot> enabled) {
        EnumSet<Slot> on = (enabled == null || enabled.isEmpty())
                ? EnumSet.allOf(Slot.class)
                : EnumSet.copyOf(enabled);

        Map<SlotKey, List<FrameSequence>> index = new HashMap<>();

        if (on.contains(Slot.A)) {
            Path aRoot = root.resolve("event").resolve("A");
            for (String g : GENDERS) {
                Gender gender = Gender.of(g);
                for (String tier : EVENT_TIERS) {
                    addStageDir(index, aRoot.resolve(g).resolve(tier),
                            ph -> SlotKey.eventA(gender, EventTier.of(tier)));
                }
            }
        }
        if (on.contains(Slot.V)) {
            for (String tier : EVENT_TIERS) {
                addStageDir(index, root.resolve("event").resolve("V").resolve(tier),
                        ph -> SlotKey.event(Slot.V, EventTier.of(tier)));
            }
        }
        if (on.contains(Slot.M)) {
            for (String tier : EVENT_TIERS) {
                addStageDir(index, root.resolve("event").resolve("M").resolve(tier),
                        ph -> SlotKey.event(Slot.M, EventTier.of(tier)));
            }
        }
        if (on.contains(Slot.BOTLE)) {
            addStageDir(index, root.resolve("event").resolve("botle"), ph -> SlotKey.botle());
        }

        if (on.contains(Slot.VFULL)) {
            for (String d : DIRECTIONS) {
                Direction dir = Direction.of(d);
                addStageDir(index, root.resolve("numeric").resolve("Vfull").resolve(d),
                        ph -> SlotKey.numeric(Slot.VFULL, dir, ph));
            }
        }
        if (on.contains(Slot.AFULL)) {
            for (String d : DIRECTIONS) {
                Direction dir = Direction.of(d);
                addStageDir(index, root.resolve("numeric").resolve("Afull").resolve(d),
                        ph -> SlotKey.numeric(Slot.AFULL, dir, ph));
            }
        }
        if (on.contains(Slot.PREGNANT)) {
            addStageDir(index, root.resolve("numeric").resolve("pregnant"), SlotKey::pregnant);
            addStageDir(index, root.resolve("numeric").resolve("pregnant").resolve("birth"), ph -> SlotKey.birth());
        }
        if (on.contains(Slot.FXXKME)) {
            addStageDir(index, root.resolve("numeric").resolve("fxxkme"), SlotKey::fxxkme);
        }

        return SlotIndex.of(index);
    }

    /**
     * 解析单个阶段目录：读取图片 → 按（前缀, [phX]）分组 → 生成候选帧序列。
     * 分组键 = "前缀@ph"（事件样式文件 ph=0），保证同前缀不同 ph 档互不串扰。
     */
    private static void addStageDir(Map<SlotKey, List<FrameSequence>> index, Path dir, KeyForStage keyFor) {
        List<Path> images = listImages(dir);
        if (images.isEmpty()) {
            return; // 缺目录/空目录：静默跳过
        }

        // groupKey("前缀@ph") -> (frameNo -> path)，帧按序号排序
        Map<String, TreeMap<Integer, Path>> sequences = new LinkedHashMap<>();
        // prefix(单图, 无 _fYY) -> 图片
        Map<String, List<Path>> singles = new LinkedHashMap<>();

        for (Path img : images) {
            String name = img.getFileName().toString();

            Matcher numeric = NUMERIC_FRAME.matcher(name);
            if (numeric.matches()) {
                sequences.computeIfAbsent(numeric.group(1) + "@" + numeric.group(2), k -> new TreeMap<>())
                        .put(Integer.parseInt(numeric.group(3)), img);
                continue;
            }

            Matcher event = EVENT_FRAME.matcher(name);
            if (event.matches()) {
                sequences.computeIfAbsent(event.group(1) + "@0", k -> new TreeMap<>())
                        .put(Integer.parseInt(event.group(2)), img);
                continue;
            }

            // 兜底：不带 _fYY 的图片按单张装载
            singles.computeIfAbsent(stripExtension(name), k -> new ArrayList<>()).add(img);
        }

        for (Map.Entry<String, TreeMap<Integer, Path>> e : sequences.entrySet()) {
            int at = e.getKey().lastIndexOf('@');
            String prefix = e.getKey().substring(0, at);
            int ph = Integer.parseInt(e.getKey().substring(at + 1));
            SlotKey key = keyFor.key(ph);
            index.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(FrameSequence.sequence(prefix, List.copyOf(e.getValue().values()), DEFAULT_FPS));
        }
        for (Map.Entry<String, List<Path>> e : singles.entrySet()) {
            SlotKey key = keyFor.key(0);
            index.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(FrameSequence.single(e.getKey(), e.getValue().get(0)));
        }
    }

    /** 目录下的图片文件列表（仅 .jpg/.png）；不可读时返回空。 */
    private static List<Path> listImages(Path dir) {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return n.endsWith(".jpg") || n.endsWith(".png");
                    })
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
