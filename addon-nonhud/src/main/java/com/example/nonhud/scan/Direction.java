package com.example.nonhud.scan;

/** 数值槽增减方向（仅 Vfull/Afull 存在此层级）。 */
public enum Direction {
    UP, RE;

    public static Direction of(String name) {
        return switch (name.toLowerCase(java.util.Locale.ROOT)) {
            case "up" -> UP;
            case "re" -> RE;
            default -> throw new IllegalArgumentException("unknown direction: " + name);
        };
    }
}
