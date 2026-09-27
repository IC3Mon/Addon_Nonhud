package com.example.nonhud.render;

import com.example.nonhud.scan.Slot;

/**
 * 渲染实例复合键（模块 5.1 分组重构）：同一槽可属于多个组，各组各一份显示实例。
 */
public record RenderKey(Slot slot, String groupId) {

    public RenderKey {
        if (slot == null || groupId == null) {
            throw new IllegalArgumentException("RenderKey slot/groupId must not be null");
        }
    }
}
