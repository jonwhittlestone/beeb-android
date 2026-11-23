// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

/** A person using this TV. Same fields as Beeb-public's BeebProfile; updatedAt is seconds since 2001-01-01. */
data class BeebProfile(val id: String, val name: String, val kids: Boolean = false, val updatedAt: Double)

class ProfileStore(private val kv: KeyValue, private val clock: () -> Double = ::nowRef, private val newId: () -> String = ::randomId) {
    val profiles: List<BeebProfile> get() = emptyList()
    val currentId: String get() = ""
    val removed: Map<String, Double> get() = emptyMap()
    var kidsPin: String = ""
    fun profile(id: String): BeebProfile? = null
    fun add(name: String): BeebProfile? = null
    fun rename(id: String, name: String) {}
    fun setKids(id: String, kids: Boolean) {}
    fun select(id: String) {}
    fun remove(id: String): Boolean = true
}

private const val REF_2001_MS = 978_307_200_000L
/** Swift's Date().timeIntervalSinceReferenceDate. */
fun nowRef() = (System.currentTimeMillis() - REF_2001_MS) / 1000.0
fun randomId() = "p" + java.util.UUID.randomUUID().toString().replace("-", "").take(10)
