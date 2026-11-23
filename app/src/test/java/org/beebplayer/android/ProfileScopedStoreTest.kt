package org.beebplayer.android

import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test

class ProfileScopedStoreTest {
    private val ep = Programme("m0000001", "Show", "Series 1: 1", showId = "b0000001")

    @Test fun savedAndHistoryArePerProfile() {
        val kv = MapKeyValue(); val profiles = ProfileStore(kv); val alice = profiles.add("Alice")!!
        val store = LocalStore(kv)
        store.toggle(ep); store.update(ep, null, 60_000, 1_800_000)
        profiles.select(alice.id)
        assertFalse(store.isSaved(ep)); assertTrue(store.history().isEmpty())
        profiles.select("main")
        assertTrue(store.isSaved(ep)); assertEquals(1, store.history().size)
    }
    @Test fun dataSavedBeforeProfilesMovesToMainOnce() {
        val kv = MapKeyValue()
        kv.putString("saved", JSONArray(listOf(ep.json())).toString())
        val store = LocalStore(kv)
        assertTrue(store.isSaved(ep))
        assertEquals(null, kv.getString("saved"))
        kv.putString("saved", "[]")        // a stray legacy write never overwrites main again
        assertTrue(LocalStore(kv).isSaved(ep))
    }
    @Test fun removingAProfileDeletesItsData() {
        val kv = MapKeyValue(); val profiles = ProfileStore(kv); val alice = profiles.add("Alice")!!
        profiles.select(alice.id); LocalStore(kv).toggle(ep)
        assertNotNull(kv.getString("saved-${alice.id}"))
        profiles.remove(alice.id)
        assertNull(kv.getString("saved-${alice.id}"))
    }
    @Test fun playbackSettingsStayDeviceWide() {
        val kv = MapKeyValue(); val profiles = ProfileStore(kv); val alice = profiles.add("Alice")!!
        LocalStore(kv).autoCaptions(false); profiles.select(alice.id)
        assertFalse(LocalStore(kv).autoCaptions())
    }
}
