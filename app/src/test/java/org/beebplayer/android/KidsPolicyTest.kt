package org.beebplayer.android

import org.junit.Assert.*
import org.junit.Test

class KidsPolicyTest {
    private val channels = listOf("bbc_one_london", "cbbc", "cbeebies", "bbc_news24").map { Channel(it, it) }
    private val calm = Programme("m0000001", "Show", "Series 1: 1")
    private val strong = Programme("m0000002", "Show", "Series 1: 2", guidance = "Contains some strong language.")

    @Test fun kidsSeeOnlyCbeebiesAndCbbc() {
        assertEquals(listOf("cbbc", "cbeebies"), KidsPolicy.channels(channels, kids = true).map { it.id })
        assertEquals(channels, KidsPolicy.channels(channels, kids = false))
    }
    @Test fun kidsNeverSeeEpisodesWithGuidance() {
        assertEquals(listOf(calm), KidsPolicy.episodes(listOf(calm, strong), kids = true))
        assertEquals(listOf(calm, strong), KidsPolicy.episodes(listOf(calm, strong), kids = false))
    }
    @Test fun kidsTabsAreHomeLiveAndSaved() {
        assertEquals(listOf("Home", "Live", "Saved"), KidsPolicy.tabs(kids = true))
        assertEquals(listOf("Home", "Browse", "Search", "Live", "Saved", "Settings"), KidsPolicy.tabs(kids = false))
    }
    @Test fun kidsHomeRowsComeFromTheChildrensCategories() {
        assertEquals(listOf("cbeebies" to "CBeebies", "cbbc" to "CBBC"), KidsPolicy.homeCategories)
    }
}
