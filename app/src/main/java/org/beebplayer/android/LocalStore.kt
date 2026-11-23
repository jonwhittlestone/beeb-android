// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Progress(val episode: Programme, val next: Programme?, val position: Long, val duration: Long, val completed: Boolean, val updated: Long) {
    val target get() = if (completed) next else episode
    fun json() = JSONObject().put("episode", episode.json()).put("next", next?.json()).put("position", position).put("duration", duration).put("completed", completed).put("updated", updated)
}
/** Saved programmes and Continue Watching for the current profile; playback settings are device-wide. */
class LocalStore(private val kv: KeyValue) {
    constructor(context: Context) : this(PrefsKeyValue(context))
    companion object {
        val PROFILE_KEYS = listOf("saved", "history")
        private const val MIGRATED_KEY = "beebProfilesMigrated"
        /** One-off, like Swift's migrateLegacy: data saved before profiles existed belongs to the default profile. */
        fun migrateLegacy(kv: KeyValue) {
            if (kv.getBool(MIGRATED_KEY, false)) return
            for (base in PROFILE_KEYS) {
                val old = kv.getString(base) ?: continue
                if (kv.getString("$base-${ProfileStore.DEFAULT_ID}") == null) kv.putString("$base-${ProfileStore.DEFAULT_ID}", old)
                kv.putString(base, null)
            }
            kv.putBool(MIGRATED_KEY, true)
        }
    }
    init { migrateLegacy(kv) }
    /** Read on every call, so a profile switch in one screen applies everywhere (as Swift's ProfileStore.currentID). */
    private fun key(base: String) = "$base-${ProfileStore.currentId(kv)}"
    fun saved(): List<Programme> = runCatching { JSONArray(kv.getString(key("saved")) ?: "[]").objects().map(Programme::restore) }.getOrDefault(emptyList())
    fun isSaved(p: Programme) = saved().any { it.id == p.id }
    fun toggle(p: Programme) {
        val all = saved().toMutableList(); if (!all.removeAll { it.id == p.id }) all.add(0, p)
        kv.putString(key("saved"), JSONArray(all.map { it.json() }).toString())
    }
    fun history(): List<Progress> = runCatching {
        JSONArray(kv.getString(key("history")) ?: "[]").objects().map { j -> Progress(Programme.restore(j.getJSONObject("episode")), j.optJSONObject("next")?.let(Programme::restore), j.optLong("position"), j.optLong("duration"), j.optBoolean("completed"), j.optLong("updated")) }.sortedByDescending { it.updated }
    }.getOrDefault(emptyList())
    fun resume(p: Programme): Long = history().firstOrNull { it.episode.id == p.id && !it.completed }?.position ?: 0
    fun update(p: Programme, next: Programme?, position: Long, duration: Long, ended: Boolean = false) {
        if (position < 5000 && !ended || duration <= 0) return
        val all = history().filterNot { it.episode.showId == p.showId }.toMutableList()
        all.add(0, Progress(p, next, position.coerceAtMost(duration), duration, Parsing.finished(position, duration, ended), System.currentTimeMillis()))
        kv.putString(key("history"), JSONArray(all.take(40).map { it.json() }).toString())
    }
    fun remove(showId: String) { kv.putString(key("history"), JSONArray(history().filterNot { it.episode.showId == showId }.map { it.json() }).toString()) }
    fun autoCaptions() = kv.getBool("captionsOnRewind", true)
    fun autoCaptions(value: Boolean) { kv.putBool("captionsOnRewind", value) }
    /** Subtitles at the start of every programme: "system" follows Android's caption setting, "on"/"off" is the viewer's last CC choice. */
    fun subtitles() = kv.getString("subtitles") ?: "system"
    fun subtitles(value: String) { kv.putString("subtitles", value) }
}
