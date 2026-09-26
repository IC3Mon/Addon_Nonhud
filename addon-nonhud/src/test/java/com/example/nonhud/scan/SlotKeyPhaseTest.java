package com.example.nonhud.scan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** SlotKey.phaseKey() ↔ fromPhaseKey() 往返一致（S2C 槽位状态载荷的载体）。 */
class SlotKeyPhaseTest {

    @Test
    void eventASlotRoundTrip() {
        SlotKey key = SlotKey.eventA(Gender.FEMALE, EventTier.PEAK);
        assertEquals("A|FEMALE|PEAK", key.phaseKey());
        assertEquals(key, SlotKey.fromPhaseKey(key.phaseKey()));
    }

    @Test
    void eventVMSlotRoundTrip() {
        SlotKey pre = SlotKey.event(Slot.V, EventTier.PRE);
        assertEquals("V|PRE", pre.phaseKey());
        assertEquals(pre, SlotKey.fromPhaseKey(pre.phaseKey()));

        SlotKey post = SlotKey.event(Slot.M, EventTier.POST);
        assertEquals("M|POST", post.phaseKey());
        assertEquals(post, SlotKey.fromPhaseKey(post.phaseKey()));
    }

    @Test
    void botleRoundTrip() {
        SlotKey key = SlotKey.botle();
        assertEquals("BOTLE|SINGLE", key.phaseKey());
        assertEquals(key, SlotKey.fromPhaseKey(key.phaseKey()));
    }

    @Test
    void numericRoundTrip() {
        SlotKey up = SlotKey.numeric(Slot.VFULL, Direction.UP, 2);
        assertEquals("VFULL|UP|2", up.phaseKey());
        assertEquals(up, SlotKey.fromPhaseKey(up.phaseKey()));

        SlotKey re = SlotKey.numeric(Slot.AFULL, Direction.RE, 1);
        assertEquals("AFULL|RE|1", re.phaseKey());
        assertEquals(re, SlotKey.fromPhaseKey(re.phaseKey()));
    }

    @Test
    void fxxkmeAndPregnantRoundTrip() {
        SlotKey fx = SlotKey.fxxkme(3);
        assertEquals("FXXKME|3", fx.phaseKey());
        assertEquals(fx, SlotKey.fromPhaseKey(fx.phaseKey()));

        SlotKey preg = SlotKey.pregnant(2);
        assertEquals("PREGNANT|2", preg.phaseKey());
        assertEquals(preg, SlotKey.fromPhaseKey(preg.phaseKey()));

        SlotKey birth = SlotKey.birth();
        assertEquals("PREGNANT|SINGLE", birth.phaseKey());
        assertEquals(birth, SlotKey.fromPhaseKey(birth.phaseKey()));
    }

    @Test
    void invalidSlotNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> SlotKey.fromPhaseKey("NOPE|PEAK"));
    }
}
