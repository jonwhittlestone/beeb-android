package org.beebplayer.android

import org.junit.Assert.*
import org.junit.Test

/** Pins LocalStore's behaviour before profiles change its keys. */
class LocalStoreTest {
    private val ep = Programme("m0000001", "Show", "Series 1: 1", showId = "b0000001")
    private val next = Programme("m0000002", "Show", "Series 1: 2", showId = "b0000001")

    @Test fun savedTogglesOnAndOff() {
        val store = LocalStore(MapKeyValue())
        store.toggle(ep); assertTrue(store.isSaved(ep))
        store.toggle(ep); assertFalse(store.isSaved(ep))
    }
    @Test fun historyKeepsOneRecordPerShowAndResumes() {
        val store = LocalStore(MapKeyValue())
        store.update(ep, next, 25_000, 1_800_000)
        assertEquals(25_000L, store.resume(ep))
        store.update(next, null, 30_000, 1_800_000)
        assertEquals(1, store.history().size)
        assertEquals(next, store.history().first().episode)
    }
    @Test fun ignoresTheFirstFiveSeconds() {
        val store = LocalStore(MapKeyValue())
        store.update(ep, next, 4_000, 1_800_000)
        assertTrue(store.history().isEmpty())
    }
    @Test fun settingsDefaults() {
        val store = LocalStore(MapKeyValue())
        assertTrue(store.autoCaptions()); assertEquals("system", store.subtitles())
        store.autoCaptions(false); store.subtitles("on")
        assertFalse(store.autoCaptions()); assertEquals("on", store.subtitles())
    }
}
