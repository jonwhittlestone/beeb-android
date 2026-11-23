// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

/** What the programme page's main button does. One sealed type gives the label and the target together. */
sealed interface StartAction {
    data class Resume(val episode: Programme, val at: Long) : StartAction
    data class Next(val episode: Programme) : StartAction
    data class Start(val episode: Programme) : StartAction
}

object StartWatching {
    /** [episodes] oldest first (as BbcApi returns them); [history] is the current profile's Continue Watching. */
    fun resolve(episodes: List<Programme>, history: List<Progress>): StartAction? {
        val first = episodes.firstOrNull() ?: return null
        val showId = first.showId
        val record = history.firstOrNull { it.episode.showId == showId } ?: return StartAction.Start(first)
        if (!record.completed) return StartAction.Resume(record.episode, record.position)
        val following = record.next ?: episodes.getOrNull(episodes.indexOfFirst { it.id == record.episode.id } + 1)?.takeIf { episodes.any { e -> e.id == record.episode.id } }
        return following?.let { StartAction.Next(it) } ?: StartAction.Start(first)
    }
}
