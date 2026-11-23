// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

/** What the programme page's main button does. One sealed type gives the label and the target together. */
sealed interface StartAction {
    data class Resume(val episode: Programme, val at: Long) : StartAction
    data class Next(val episode: Programme) : StartAction
    data class Start(val episode: Programme) : StartAction
}

object StartWatching {
    fun resolve(episodes: List<Programme>, history: List<Progress>): StartAction? = null
}
