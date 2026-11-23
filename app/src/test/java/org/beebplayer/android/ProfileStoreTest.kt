package org.beebplayer.android

import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test

class ProfileStoreTest {
    private var clock = 100.0
    private var n = 0
    private fun store(kv: KeyValue = MapKeyValue()) = ProfileStore(kv, clock = { clock }, newId = { "p${++n}" })

    @Test fun seedsTheMainProfileCalledMe() {
        val s = store()
        assertEquals(listOf(BeebProfile("main", "Me", kids = false, updatedAt = 0.0)), s.profiles)
        assertEquals("main", s.currentId)
    }
    @Test fun addTrimsCapsAndRejectsBlankNames() {
        val s = store()
        assertNull(s.add("   "))
        val alice = s.add("  Alice  ")!!
        assertEquals(BeebProfile("p1", "Alice", false, 100.0), alice)
        assertEquals(30, s.add("x".repeat(40))!!.name.length)
        assertEquals(listOf("main", "p1", "p2"), s.profiles.map { it.id })
    }
    @Test fun renameAndKidsFlagBumpUpdatedAt() {
        val s = store(); val p = s.add("Jo")!!
        clock = 200.0
        s.rename(p.id, "Joseph"); s.setKids(p.id, true)
        assertEquals(BeebProfile(p.id, "Joseph", true, 200.0), s.profile(p.id))
    }
    @Test fun neverRemovesTheLastProfile() {
        val s = store()
        assertFalse(s.remove("main"))
        assertEquals(1, s.profiles.size)
    }
    @Test fun removingTheCurrentProfileSelectsTheFirstAndRecordsATombstone() {
        val kv = MapKeyValue(); val s = store(kv); val p = s.add("Alice")!!
        s.select(p.id); clock = 300.0
        assertTrue(s.remove(p.id))
        assertEquals("main", s.currentId)
        assertEquals(300.0, s.removed[p.id]!!, 0.0)
    }
    @Test fun persistsInSwiftCompatibleJson() {
        val kv = MapKeyValue(); val s = store(kv); s.add("Alice"); s.select("p1")
        val json = JSONArray(kv.getString("beebProfiles-v1")).getJSONObject(1)
        assertEquals(setOf("id", "name", "kids", "updatedAt"), json.keys().asSequence().toSet())
        assertEquals("p1", kv.getString("beebProfile"))
        val reloaded = store(kv)
        assertEquals(s.profiles, reloaded.profiles); assertEquals("p1", reloaded.currentId)
    }
    @Test fun kidsPinIsStoredPlainLikeSwift() {
        val kv = MapKeyValue(); val s = store(kv)
        s.kidsPin = "1234"
        assertEquals("1234", kv.getString("beebKidsPIN"))
        assertEquals("1234", store(kv).kidsPin)
    }
}
