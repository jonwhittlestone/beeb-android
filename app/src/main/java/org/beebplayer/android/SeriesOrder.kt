// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

/** Programme-page ordering, as Beeb-public 0.11.0: newest series first, newest episode first, Specials last. */
object SeriesOrder {
    private const val SPECIALS = "Episodes"
    private val numbered = Regex("(?:Series|Season)\\s+(\\d+)", RegexOption.IGNORE_CASE)

    fun groups(episodes: List<Programme>): List<Pair<String, List<Programme>>> =
        episodes.groupBy { it.series }.toList()
            .sortedWith(seriesNewestFirst)
            .map { (series, items) -> series to items.sortedWith(episodeNewestFirst) }

    private fun number(series: String) = numbered.find(series)?.groupValues?.get(1)?.toIntOrNull()
    private fun latest(items: List<Programme>) = items.maxOfOrNull { it.date }.orEmpty()

    /** Specials last; numbered series by number; otherwise by latest broadcast (ISO dates sort as text). */
    private val seriesNewestFirst = Comparator<Pair<String, List<Programme>>> { (a, ai), (b, bi) ->
        val na = number(a); val nb = number(b)
        when {
            (a == SPECIALS) != (b == SPECIALS) -> if (a == SPECIALS) 1 else -1
            na != null && nb != null -> nb.compareTo(na)
            else -> latest(bi).compareTo(latest(ai))
        }
    }
    /** BBC episode position when both have one, else broadcast date. */
    private val episodeNewestFirst = Comparator<Programme> { a, b ->
        if (a.position > 0 && b.position > 0) b.position.compareTo(a.position) else b.date.compareTo(a.date)
    }
}
