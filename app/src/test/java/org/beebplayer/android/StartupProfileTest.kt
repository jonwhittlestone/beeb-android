package org.beebplayer.android

import org.junit.Assert.*
import org.junit.Test

class StartupProfileTest {
    private fun twoProfiles(kv: KeyValue = MapKeyValue()) = ProfileStore(kv).also { it.add("Alice") }

    @Test fun defaultIsTheLastProfileUsed() {
        val kv = MapKeyValue(); val s = twoProfiles(kv); val alice = s.profiles[1]
        assertEquals("last", s.startup)
        s.select(alice.id)
        val next = ProfileStore(kv)
        assertFalse(next.launch())
        assertEquals(alice.id, next.currentId)
    }
    @Test fun alwaysAProfileOverridesTheLastUsed() {
        val kv = MapKeyValue(); val s = twoProfiles(kv); val alice = s.profiles[1]
        s.startup = alice.id; s.select("main")
        val next = ProfileStore(kv)
        assertFalse(next.launch())
        assertEquals(alice.id, next.currentId)
    }
    @Test fun askNeedsThePickerOnlyWithMoreThanOneProfile() {
        val kv = MapKeyValue(); val s = ProfileStore(kv)
        s.startup = "ask"
        assertFalse(ProfileStore(kv).launch())
        s.add("Alice")
        assertTrue(ProfileStore(kv).launch())
    }
    @Test fun removingTheAlwaysProfileFallsBackToLast() {
        val s = twoProfiles(); val alice = s.profiles[1]
        s.startup = alice.id; s.remove(alice.id)
        assertEquals("last", s.startup)
    }
}
