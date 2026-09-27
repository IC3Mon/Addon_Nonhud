package com.example.nonhud.engine;

import com.example.nonhud.ChatDebug;
import com.example.nonhud.NoNHudRuntime;
import com.example.nonhud.net.SlotSyncPacket.SlotHidePayload;
import com.example.nonhud.net.SlotSyncPacket.SlotShowPayload;
import com.example.nonhud.scan.Direction;
import com.example.nonhud.scan.Slot;
import com.example.nonhud.scan.SlotKey;
import com.nonid.api.NonEntityApi;
import com.nonid.api.NonLiquidTankSnapshot;
import com.nonid.api.NonPregnancySnapshot;
import com.nonid.api.NonPlayerApi;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * 模块 2/3：numeric 槽触发适配层（服务端 tick 采样，设计 §3.3 numeric 段）。
 *
 * 每玩家状态采样：energy（NonEntityApi，0–200）→ fxxkme；液体槽按性别路由
 * （female→Vfull、male→Afull，NumericTier.tankSlotFor）→ up/re × phX；
 * 妊娠（NonPregnancySnapshot.endTick 反推 0–255 进度，PregnancyProgress）→ phX。
 * 穿越阈值（档位变化）才驱动 TriggerEngine 并 S2C 下发。
 *
 * 阈值默认值来自设计文档 §6（模块 5 配置落地后由 Config 注入）。
 */
public final class NumericMonitor {

    /** 设计 §6 thresholds 默认值（V槽 [30,60,100]：100% 满值 → 档位四）。 */
    static final List<Integer> ENERGY_THRESHOLDS = List.of(50, 100, 150);
    static final List<Integer> V_TANK_THRESHOLDS = List.of(30, 60, 100);
    static final List<Integer> A_TANK_THRESHOLDS = List.of(30, 60, 100);
    static final List<Integer> PREGNANCY_THRESHOLDS = List.of(85, 170, 255);

    private static List<Integer> energyThresholds = ENERGY_THRESHOLDS;
    private static List<Integer> vTankThresholds = V_TANK_THRESHOLDS;
    private static List<Integer> aTankThresholds = A_TANK_THRESHOLDS;
    private static List<Integer> pregnancyThresholds = PREGNANCY_THRESHOLDS;

    /** 数值槽一次性触发播放时长（毫秒；默认 3s，模块 5 配置注入；0 = 一直显示）。 */
    private static long displayDurationMs = 3000;

    /** fxxkme 周期触发（模块 5.1）：energy ≥ 阈值后每随机间隔播放一次 ph3。 */
    private static int fxxkmeEnergyThreshold = 200;
    private static int fxxkmeIntervalMinSec = 30;
    private static int fxxkmeIntervalMaxSec = 60;

    /** 模块 5 配置注入：数值槽档位阈值（空列表 = 永不触发，容错）。 */
    public static void setThresholds(List<Integer> energy, List<Integer> vTank,
                                     List<Integer> aTank, List<Integer> pregnancy) {
        energyThresholds = List.copyOf(energy);
        vTankThresholds = List.copyOf(vTank);
        aTankThresholds = List.copyOf(aTank);
        pregnancyThresholds = List.copyOf(pregnancy);
    }

    /** 模块 5 配置注入：fxxkme 周期触发参数（energy 阈值 + 随机间隔秒数区间）。 */
    public static void setFxxkmeParams(int energyThreshold, int intervalMinSec, int intervalMaxSec) {
        fxxkmeEnergyThreshold = Math.max(0, energyThreshold);
        fxxkmeIntervalMinSec = Math.max(1, intervalMinSec);
        fxxkmeIntervalMaxSec = Math.max(fxxkmeIntervalMinSec, intervalMaxSec);
    }

    /** 模块 5 配置注入：数值槽一次性触发播放时长（毫秒；0 = 一直显示）。 */
    public static void setDisplayDurationMs(long ms) {
        displayDurationMs = Math.max(0, ms);
    }

    private static final Map<UUID, PlayerNumericState> STATES = new HashMap<>();
    private static final Random RANDOM = new Random();

    private NumericMonitor() { }

    private static final class PlayerNumericState {
        final NumericTracker tank = new NumericTracker();
        NumericTracker pregnancy = new NumericTracker();
        PregnancyProgress pregnancyProgress = new PregnancyProgress();
        Slot lastTankSlot;
        boolean wasPregnant;
        /** fxxkme 周期触发（模块 5.1）：下一次触发 tick（0 = 未就绪）。 */
        long fxxkmeNextTick;
        /** 数值槽显示截止 tick（一次性触发到期自动隐藏；key=槽位）。 */
        final EnumMap<Slot, Long> hideDeadlineTicks = new EnumMap<>(Slot.class);
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(NumericMonitor::tick);
    }

    private static void tick(MinecraftServer server) {
        long now = server.getOverworld().getTime();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerNumericState state = STATES.computeIfAbsent(player.getUuid(), k -> new PlayerNumericState());
            sampleEnergy(player, state, now);
            sampleTank(player, state, now);
            samplePregnancy(player, state, now, false);
            expireDue(player, state, now);
            TriggerEngineHub.getFor(player.getUuid()).tick(1);
        }
    }

    /** 出生 cue 结束后恢复妊娠进度分档（无条件重算并下发）。 */
    static void resamplePregnancy(ServerPlayerEntity player) {
        PlayerNumericState state = STATES.computeIfAbsent(player.getUuid(), k -> new PlayerNumericState());
        long now = player.getEntityWorld().getTime();
        samplePregnancy(player, state, now, true);
    }

    private static void sampleEnergy(ServerPlayerEntity player, PlayerNumericState state, long nowTicks) {
        TriggerEngine engine = TriggerEngineHub.getFor(player.getUuid());
        if (!NonEntityApi.hasEnergy(player)) {
            hideFxxkme(player, state, engine);
            return;
        }
        int value = NonEntityApi.getEnergy(player).energy();
        if (value < fxxkmeEnergyThreshold) {
            hideFxxkme(player, state, engine);
            return;
        }
        // 模块 5.1：energy ≥ 阈值后周期性一次性触发 fxxkme ph3（随机间隔可配置）
        if (state.fxxkmeNextTick == 0 || nowTicks >= state.fxxkmeNextTick) {
            SlotKey key = SlotKey.fxxkme(3);
            engine.show(Slot.FXXKME, key, NoNHudRuntime.index().get(key));
            sendShown(player, state, engine, Slot.FXXKME, nowTicks);
            int range = fxxkmeIntervalMaxSec - fxxkmeIntervalMinSec + 1;
            int intervalSec = fxxkmeIntervalMinSec + RANDOM.nextInt(Math.max(1, range));
            state.fxxkmeNextTick = nowTicks + intervalSec * 20L;
            ChatDebug.server(player, "[numeric] energy≥" + fxxkmeEnergyThreshold + "，周期触发 fxxkme ph3，下次 "
                    + intervalSec + "s 后");
        }
    }

    /** energy 未达标/无 energy：隐藏 fxxkme 并复位周期计时。 */
    private static void hideFxxkme(ServerPlayerEntity player, PlayerNumericState state, TriggerEngine engine) {
        if (engine.isActive(Slot.FXXKME)) {
            engine.hide(Slot.FXXKME);
            ServerPlayNetworking.send(player, new SlotHidePayload(Slot.FXXKME.name()));
        }
        state.hideDeadlineTicks.remove(Slot.FXXKME);
        state.fxxkmeNextTick = 0;
    }

    private static void sampleTank(ServerPlayerEntity player, PlayerNumericState state, long nowTicks) {
        TriggerEngine engine = TriggerEngineHub.getFor(player.getUuid());
        if (!NonPlayerApi.isLiquidTankEnabled()) {
            return;
        }
        Slot tankSlot = NumericTier.tankSlotFor(
                NonPlayerApi.getGender(player) == com.nonid.api.NonGender.MALE
                        ? com.example.nonhud.scan.Gender.MALE
                        : com.example.nonhud.scan.Gender.FEMALE);
        if (tankSlot == null) {
            return;
        }
        if (tankSlot != state.lastTankSlot) {
            state.lastTankSlot = tankSlot;
            state.tank.reset();
        }
        NonLiquidTankSnapshot tank = NonPlayerApi.getLiquidTank(player);
        int fill = tank.capacityMl() > 0
                ? Math.round(100f * tank.storedMl() / tank.capacityMl())
                : 0;
        List<Integer> thresholds = tankSlot == Slot.AFULL ? aTankThresholds : vTankThresholds;
        Optional<NumericTracker.Change> change = state.tank.update(fill, thresholds);
        if (change.isEmpty()) {
            return;
        }
        Direction dir = change.get().direction() != null ? change.get().direction() : Direction.UP;
        SlotKey key = SlotKey.numeric(tankSlot, dir, change.get().ph());
        engine.show(tankSlot, key, NoNHudRuntime.index().get(key));
        sendShown(player, state, engine, tankSlot, nowTicks);
    }

    private static void samplePregnancy(ServerPlayerEntity player, PlayerNumericState state,
                                        long nowTicks, boolean force) {
        TriggerEngine engine = TriggerEngineHub.getFor(player.getUuid());
        if (!NonPlayerApi.isPregnancyEnabled()) {
            return;
        }
        NonPregnancySnapshot snap = NonPlayerApi.getPregnancy(player);
        if (!snap.active()) {
            if (state.wasPregnant) {
                if (engine.isActive(Slot.PREGNANT)) {
                    engine.hide(Slot.PREGNANT);
                    ServerPlayNetworking.send(player, new SlotHidePayload(Slot.PREGNANT.name()));
                }
                state.hideDeadlineTicks.remove(Slot.PREGNANT);
                state.wasPregnant = false;
            }
            return;
        }
        state.wasPregnant = true;
        long remaining = snap.endTick() - player.getEntityWorld().getTime();
        int progress = state.pregnancyProgress.progress255(remaining);
        Optional<NumericTracker.Change> change = state.pregnancy.update(progress, pregnancyThresholds);
        if (change.isEmpty() && !force) {
            return;
        }
        int ph = change.map(NumericTracker.Change::ph).orElse(state.pregnancy.currentPh());
        SlotKey key = SlotKey.pregnant(ph);
        engine.show(Slot.PREGNANT, key, NoNHudRuntime.index().get(key));
        sendShown(player, state, engine, Slot.PREGNANT, nowTicks);
    }

    /** 若引擎最终未显示（目录缺图）则下发隐藏，避免客户端残留；显示则登记一次性触发截止 tick。 */
    private static void sendShown(ServerPlayerEntity player, PlayerNumericState state,
                                  TriggerEngine engine, Slot slot, long nowTicks) {
        SlotKey shown = engine.current(slot).map(TriggerEngine.Displayed::key).orElse(null);
        if (shown != null) {
            ServerPlayNetworking.send(player, new SlotShowPayload(shown.phaseKey()));
            if (displayDurationMs > 0) {
                long durationTicks = Math.max(1, displayDurationMs * 20 / 1000);
                state.hideDeadlineTicks.put(slot, nowTicks + durationTicks);
            }
        } else {
            ServerPlayNetworking.send(player, new SlotHidePayload(slot.name()));
            state.hideDeadlineTicks.remove(slot);
        }
    }

    /** 播放时长到期：隐藏仍显示的数值槽并下发隐藏（一次性触发，不随数值常驻）。 */
    private static void expireDue(ServerPlayerEntity player, PlayerNumericState state, long nowTicks) {
        if (state.hideDeadlineTicks.isEmpty()) {
            return;
        }
        TriggerEngine engine = TriggerEngineHub.getFor(player.getUuid());
        // 先收集到期的槽位，再统一移除，避免在遍历 EnumMap 时修改 map（迭代器失效抛异常）
        List<Slot> expired = new ArrayList<>();
        for (Iterator<Map.Entry<Slot, Long>> it = state.hideDeadlineTicks.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Slot, Long> e = it.next();
            if (nowTicks >= e.getValue()) {
                expired.add(e.getKey());
            }
        }
        for (Slot slot : expired) {
            state.hideDeadlineTicks.remove(slot);
            if (engine.isActive(slot)) {
                engine.hide(slot);
                ChatDebug.server(player, "[numeric] 槽位 " + slot + " 播放时长到期，隐藏");
                ServerPlayNetworking.send(player, new SlotHidePayload(slot.name()));
            }
        }
    }
}
