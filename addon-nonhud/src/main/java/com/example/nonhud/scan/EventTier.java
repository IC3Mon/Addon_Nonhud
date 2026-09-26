package com.example.nonhud.scan;

/** 事件槽阶段档位（pre/peak/post）；SINGLE 用于单档槽（botle、pregnant/birth）。 */
public enum EventTier {
    PRE, PEAK, POST, SINGLE;

    public static EventTier of(String name) {
        return switch (name.toLowerCase(java.util.Locale.ROOT)) {
            case "pre" -> PRE;
            case "peak" -> PEAK;
            case "post" -> POST;
            case "single" -> SINGLE;
            default -> throw new IllegalArgumentException("unknown event tier: " + name);
        };
    }
}
