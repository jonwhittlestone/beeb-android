// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

import org.json.JSONArray
import org.json.JSONObject

/** A person using this TV. Same fields as Beeb-public's BeebProfile; updatedAt is seconds since 2001-01-01. */
data class BeebProfile(val id: String, val name: String, val kids: Boolean = false, val updatedAt: Double)

/**
 * The profiles on this TV, ported from Beeb-public's ProfileStore (Profiles.swift, v0.11.0).
 * Keys and JSON match Swift so a household-sync client can use the same server later.
 */
class ProfileStore(private val kv: KeyValue, private val clock: () -> Double = ::nowRef, private val newId: () -> String = ::randomId) {
    companion object {
        const val DEFAULT_ID = "main"
        const val LIST_KEY = "beebProfiles-v1"
        const val REMOVED_KEY = "beebProfilesRemoved-v1"
        const val CURRENT_KEY = "beebProfile"
        const val PIN_KEY = "beebKidsPIN"
        const val MAX_NAME = 30
    }
    var profiles: List<BeebProfile> = load(); private set
    var removed: Map<String, Double> = loadRemoved(); private set
    var currentId: String = (kv.getString(CURRENT_KEY) ?: DEFAULT_ID).takeIf { id -> profiles.any { it.id == id } } ?: profiles.first().id
        private set
    var kidsPin: String
        get() = kv.getString(PIN_KEY) ?: ""
        set(value) = kv.putString(PIN_KEY, value)
    val current: BeebProfile get() = profile(currentId) ?: profiles.first()

    fun profile(id: String) = profiles.firstOrNull { it.id == id }

    fun add(name: String): BeebProfile? {
        val clean = name.trim().take(MAX_NAME).ifEmpty { return null }
        return BeebProfile(newId(), clean, updatedAt = clock()).also { profiles = profiles + it; save() }
    }
    fun rename(id: String, name: String) {
        val clean = name.trim().take(MAX_NAME).ifEmpty { return }
        edit(id) { it.copy(name = clean) }
    }
    fun setKids(id: String, kids: Boolean) { if (profile(id)?.kids != kids) edit(id) { it.copy(kids = kids) } }
    fun select(id: String) { if (profile(id) != null) { currentId = id; kv.putString(CURRENT_KEY, id) } }
    /** Removes a profile and records a tombstone for sync. The last profile can't be removed. */
    fun remove(id: String): Boolean {
        if (profiles.size <= 1 || profile(id) == null) return false
        profiles = profiles.filterNot { it.id == id }
        removed = removed + (id to clock())
        if (currentId == id) select(profiles.first().id)
        save(); return true
    }

    private fun edit(id: String, change: (BeebProfile) -> BeebProfile) {
        profiles = profiles.map { if (it.id == id) change(it).copy(updatedAt = clock()) else it }; save()
    }
    private fun save() {
        kv.putString(LIST_KEY, JSONArray(profiles.map { JSONObject().put("id", it.id).put("name", it.name).put("kids", it.kids).put("updatedAt", it.updatedAt) }).toString())
        kv.putString(REMOVED_KEY, JSONObject(removed).toString())
    }
    private fun load(): List<BeebProfile> = runCatching {
        JSONArray(kv.getString(LIST_KEY)).objects().map { BeebProfile(it.text("id"), it.text("name"), it.optBoolean("kids"), it.optDouble("updatedAt", 0.0)) }
    }.getOrNull()?.takeIf { it.isNotEmpty() } ?: listOf(BeebProfile(DEFAULT_ID, "Me", updatedAt = 0.0))
    private fun loadRemoved(): Map<String, Double> = runCatching {
        val j = JSONObject(kv.getString(REMOVED_KEY)); j.keys().asSequence().associateWith { j.getDouble(it) }
    }.getOrDefault(emptyMap())
}

private const val REF_2001_MS = 978_307_200_000L
/** Swift's Date().timeIntervalSinceReferenceDate. */
fun nowRef() = (System.currentTimeMillis() - REF_2001_MS) / 1000.0
fun randomId() = "p" + java.util.UUID.randomUUID().toString().replace("-", "").take(10)
