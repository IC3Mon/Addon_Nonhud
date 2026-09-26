package com.example.nonhud.render;

/**
 * 槽位显示区域（缩放像素坐标，x 左、y 上、向右向下；设计文档 §4.4「显示区域」）。
 * 模块 4 用绝对值矩形；模块 5 配置化后可由设置界面写入。
 */
public record Region(int x, int y, int width, int height) {

    public Region {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Region width/height must be >= 0");
        }
    }

    /** 与另一区域是否重叠（含边界相切不算重叠）。 */
    public boolean overlaps(Region other) {
        return x < other.x + other.width && other.x < x + width
                && y < other.y + other.height && other.y < y + height;
    }
}
