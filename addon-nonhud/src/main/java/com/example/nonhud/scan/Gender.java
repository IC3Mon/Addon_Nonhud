package com.example.nonhud.scan;

/** 玩家性别（仅 event/A 槽存在此层级）。 */
public enum Gender {
    FEMALE, MALE;

    public static Gender of(String name) {
        return switch (name.toLowerCase(java.util.Locale.ROOT)) {
            case "female" -> FEMALE;
            case "male" -> MALE;
            default -> throw new IllegalArgumentException("unknown gender: " + name);
        };
    }
}
