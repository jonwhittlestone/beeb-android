// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

/** What a kids profile may see, as Beeb-public 0.11.0. Screens ask this; they never check `kids` themselves. */
object KidsPolicy {
    val allTabs = listOf("Home", "Browse", "Search", "Live", "Saved", "Settings")
    private val kidsTabs = listOf("Home", "Live", "Saved")
    private val kidsChannels = setOf("cbeebies", "cbbc")
    /** Kids home rows: BBC category id to row title. */
    val homeCategories = listOf("cbeebies" to "CBeebies", "cbbc" to "CBBC")

    fun channels(list: List<Channel>, kids: Boolean) = if (kids) list.filter { it.id in kidsChannels } else list
    /** Hides anything carrying a BBC guidance warning. */
    fun episodes(list: List<Programme>, kids: Boolean) = if (kids) list.filter { it.guidance.isBlank() } else list
    fun tabs(kids: Boolean) = if (kids) kidsTabs else allTabs
}
