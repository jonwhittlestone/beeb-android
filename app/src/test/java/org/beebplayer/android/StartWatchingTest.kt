package org.beebplayer.android

import org.junit.Assert.*
import org.junit.Test

class StartWatchingTest {
    private fun ep(n: Int) = Programme("m000000$n", "Show", "Series 1: $n", showId = "b0000001", series = "Series 1", position = n)
    private val episodes = listOf(ep(1), ep(2), ep(3))   // oldest first, as BbcApi returns them
    private fun progress(p: Programme, completed: Boolean, next: Programme? = null) = Progress(p, next, 600_000, 1_800_000, completed, 1L)

    @Test fun nothingWatchedStartsFromTheFirstEpisode() {
        assertEquals(StartAction.Start(ep(1)), StartWatching.resolve(episodes, emptyList()))
    }
    @Test fun halfWatchedResumesWhereItStopped() {
        assertEquals(StartAction.Resume(ep(2), 600_000), StartWatching.resolve(episodes, listOf(progress(ep(2), completed = false))))
    }
    @Test fun finishedEpisodeOffersTheNextOne() {
        assertEquals(StartAction.Next(ep(3)), StartWatching.resolve(episodes, listOf(progress(ep(2), completed = true, next = ep(3)))))
    }
    @Test fun finishedWithoutAStoredNextUsesTheFollowingEpisode() {
        assertEquals(StartAction.Next(ep(2)), StartWatching.resolve(episodes, listOf(progress(ep(1), completed = true))))
    }
    @Test fun finishedTheLastEpisodeStartsAgain() {
        assertEquals(StartAction.Start(ep(1)), StartWatching.resolve(episodes, listOf(progress(ep(3), completed = true))))
    }
    @Test fun otherShowsHistoryIsIgnored() {
        val other = Programme("m0000009", "Other", showId = "b0000009")
        assertEquals(StartAction.Start(ep(1)), StartWatching.resolve(episodes, listOf(progress(other, completed = false))))
    }
    @Test fun noEpisodesNoAction() {
        assertNull(StartWatching.resolve(emptyList(), emptyList()))
    }
}
