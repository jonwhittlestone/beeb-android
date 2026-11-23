package org.beebplayer.android

import org.junit.Assert.*
import org.junit.Test

class SeriesOrderTest {
    private fun ep(id: String, series: String, position: Int = 0, date: String = "") = Programme(id, "Show", "$series: $id", series = series, position = position, date = date)

    @Test fun newestSeriesFirstAndSpecialsLast() {
        val eps = listOf(ep("a", "Series 1", 1), ep("s", "Episodes"), ep("b", "Series 2", 1), ep("c", "Series 10", 1))
        assertEquals(listOf("Series 10", "Series 2", "Series 1", "Episodes"), SeriesOrder.groups(eps).map { it.first })
    }
    @Test fun newestEpisodeFirstWithinASeries() {
        val eps = listOf(ep("a", "Series 1", 1), ep("c", "Series 1", 3), ep("b", "Series 1", 2))
        assertEquals(listOf("c", "b", "a"), SeriesOrder.groups(eps).single().second.map { it.id })
    }
    @Test fun dailySoapsWithoutPositionsOrderByBroadcastDate() {
        val eps = listOf(ep("mon", "Episodes", date = "2026-11-16T00:00:00Z"), ep("thu", "Episodes", date = "2026-11-19T00:00:00Z"), ep("tue", "Episodes", date = "2026-11-17T00:00:00Z"))
        assertEquals(listOf("thu", "tue", "mon"), SeriesOrder.groups(eps).single().second.map { it.id })
    }
    @Test fun unnumberedSeriesFollowTheirLatestBroadcast() {
        val eps = listOf(ep("x", "Christmas", 1, "2024-12-25"), ep("y", "Summer", 1, "2026-07-01"))
        assertEquals(listOf("Summer", "Christmas"), SeriesOrder.groups(eps).map { it.first })
    }
}
