package com.example.nonhud.render;

/** 缩放/拉伸方式（设计文档 §4.4「缩放 / 拉伸显示」）：等比例适配 vs 拉伸填满。 */
public enum ScaleMode {
    /** 等比例缩放适配区域内（保持宽高比，居中），不拉伸。 */
    FIT,
    /** 拉伸填满整个区域（可能变形）。 */
    STRETCH
}
