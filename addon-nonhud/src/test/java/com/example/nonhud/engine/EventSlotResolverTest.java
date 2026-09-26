package com.example.nonhud.engine;

import com.example.nonhud.scan.Gender;
import com.example.nonhud.scan.Slot;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 事件槽决策（设计 §3.3 A 槽性别强判 + §3.4 injector 角色，多角色并行）。 */
class EventSlotResolverTest {

    @Test
    void botleWinsOverAllRoles() {
        Set<EventSlotResolver.Resolved> r = EventSlotResolver.resolve(
                Set.of("A", "V", "M"), true, Gender.FEMALE);
        assertEquals(Set.of(EventSlotResolver.Resolved.slot(Slot.BOTLE, null)), r);
    }

    @Test
    void injectorAGoesToASlotWithGender() {
        Set<EventSlotResolver.Resolved> female = EventSlotResolver.resolve(
                Set.of("A"), false, Gender.FEMALE);
        assertEquals(Set.of(EventSlotResolver.Resolved.slot(Slot.A, Gender.FEMALE)), female);

        Set<EventSlotResolver.Resolved> male = EventSlotResolver.resolve(
                Set.of("A"), false, Gender.MALE);
        assertEquals(Set.of(EventSlotResolver.Resolved.slot(Slot.A, Gender.MALE)), male);
    }

    @Test
    void maleReceiverWithVForcesASlotMale() {
        Set<EventSlotResolver.Resolved> r = EventSlotResolver.resolve(
                Set.of("V"), false, Gender.MALE);
        assertEquals(Set.of(EventSlotResolver.Resolved.slot(Slot.A, Gender.MALE)), r);
    }

    @Test
    void femaleReceiverWithVGoesToVSlot() {
        Set<EventSlotResolver.Resolved> r = EventSlotResolver.resolve(
                Set.of("V"), false, Gender.FEMALE);
        assertEquals(Set.of(EventSlotResolver.Resolved.slot(Slot.V, null)), r);
    }

    @Test
    void injectorMGoesToMSlot() {
        Set<EventSlotResolver.Resolved> r = EventSlotResolver.resolve(
                Set.of("M"), false, Gender.FEMALE);
        assertEquals(Set.of(EventSlotResolver.Resolved.slot(Slot.M, null)), r);
    }

    @Test
    void aAndVBothPresentDedupForMale() {
        // README 1424：A 槽已被占时不做 V→A 重复添加；男角色 V 转 A/male 与 A 去重
        Set<EventSlotResolver.Resolved> r = EventSlotResolver.resolve(
                Set.of("V", "A"), false, Gender.MALE);
        assertEquals(Set.of(EventSlotResolver.Resolved.slot(Slot.A, Gender.MALE)), r);
    }

    @Test
    void multiRoleResolvesAllSlots() {
        // 3bee_fuck 类动画 injector=[A,M,V]：三个槽位同时显示
        Set<EventSlotResolver.Resolved> r = EventSlotResolver.resolve(
                Set.of("A", "M", "V"), false, Gender.FEMALE);
        assertEquals(Set.of(
                EventSlotResolver.Resolved.slot(Slot.A, Gender.FEMALE),
                EventSlotResolver.Resolved.slot(Slot.V, null),
                EventSlotResolver.Resolved.slot(Slot.M, null)), r);
    }

    @Test
    void amBothResolvesTwoSlots() {
        Set<EventSlotResolver.Resolved> r = EventSlotResolver.resolve(
                Set.of("A", "M"), false, Gender.FEMALE);
        assertEquals(Set.of(
                EventSlotResolver.Resolved.slot(Slot.A, Gender.FEMALE),
                EventSlotResolver.Resolved.slot(Slot.M, null)), r);
    }

    @Test
    void noInjectorMeansNone() {
        Set<EventSlotResolver.Resolved> r = EventSlotResolver.resolve(
                Set.of(), false, Gender.FEMALE);
        assertTrue(r.isEmpty());
    }
}
