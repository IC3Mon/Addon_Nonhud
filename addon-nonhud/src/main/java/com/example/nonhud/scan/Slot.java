package com.example.nonhud.scan;

/** 槽位（设计文档 §3.1 槽位与触发源总表）。 */
public enum Slot {
    A, V, M, BOTLE,
    VFULL, AFULL, PREGNANT, FXXKME;

    /** 是否为数值槽：数值触发一次性显示（播放时长后自动隐藏）；事件槽在动画期间持续显示。 */
    public boolean numeric() {
        return this == VFULL || this == AFULL || this == PREGNANT || this == FXXKME;
    }
}
