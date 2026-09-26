package com.example.nonhud.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 按玩家 UUID 持有各自的 TriggerEngine（多玩家互不串扰；单机集成服只有一个玩家）。
 * 服务端适配层（EventSlotListener / NumericMonitor）通过本中心读写每玩家槽状态。
 */
public final class TriggerEngineHub {

    private static final Map<UUID, TriggerEngine> ENGINES = new HashMap<>();

    private TriggerEngineHub() { }

    public static TriggerEngine getFor(UUID playerUuid) {
        return ENGINES.computeIfAbsent(playerUuid, uuid -> new TriggerEngine());
    }

    public static void remove(UUID playerUuid) {
        ENGINES.remove(playerUuid);
    }

    public static Collection<TriggerEngine> all() {
        return ENGINES.values();
    }
}
