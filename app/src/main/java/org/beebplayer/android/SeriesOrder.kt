// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

/** Programme-page ordering, as Beeb-public 0.11.0: newest series first, newest episode first, Specials last. */
object SeriesOrder {
    fun groups(episodes: List<Programme>): List<Pair<String, List<Programme>>> = episodes.groupBy { it.series }.toList()
}
