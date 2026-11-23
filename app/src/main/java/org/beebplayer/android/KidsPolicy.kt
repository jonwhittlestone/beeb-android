// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

/** What a kids profile may see, as Beeb-public 0.11.0. Screens ask this; they never check `kids` themselves. */
object KidsPolicy {
    val allTabs = listOf("Home", "Browse", "Search", "Live", "Saved", "Settings")
    val homeCategories: List<Pair<String, String>> = emptyList()
    fun channels(list: List<Channel>, kids: Boolean) = list
    fun episodes(list: List<Programme>, kids: Boolean) = list
    fun tabs(kids: Boolean) = allTabs
}
