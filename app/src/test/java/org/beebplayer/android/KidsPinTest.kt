package org.beebplayer.android

import org.junit.Assert.*
import org.junit.Test

class KidsPinTest {
    private val s = ProfileStore(MapKeyValue())
    private val kid = s.add("Joseph")!!.also { s.setKids(it.id, true) }
    private val adult = s.add("Alice")!!

    @Test fun noPinMeansNoGate() {
        s.select(kid.id)
        assertFalse(s.needsPin(adult.id, pickerRequired = false))
    }
    @Test fun leavingAKidsProfileForAnAdultNeedsThePin() {
        s.kidsPin = "1234"; s.select(kid.id)
        assertTrue(s.needsPin(adult.id, pickerRequired = false))
        assertTrue(s.needsPin("main", pickerRequired = false))
    }
    @Test fun movingToAnotherKidsProfileNeverNeedsThePin() {
        s.kidsPin = "1234"; s.select(kid.id)
        val other = s.add("Rosie")!!.also { s.setKids(it.id, true) }
        assertFalse(s.needsPin(other.id, pickerRequired = false))
    }
    @Test fun adultsSwitchFreelyExceptFromTheLaunchPicker() {
        s.kidsPin = "1234"; s.select(adult.id)
        assertFalse(s.needsPin("main", pickerRequired = false))
        assertTrue(s.needsPin("main", pickerRequired = true))
    }
    @Test fun aValidPinIsFourDigits() {
        assertTrue(ProfileStore.validPin("0420"))
        assertFalse(ProfileStore.validPin("123")); assertFalse(ProfileStore.validPin("12a4")); assertFalse(ProfileStore.validPin("12345"))
    }
}
