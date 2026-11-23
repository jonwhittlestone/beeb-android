// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

import android.content.Context
import android.content.SharedPreferences

/** The storage seam: SharedPreferences on the TV, a plain map in JVM unit tests. */
interface KeyValue {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
    fun getBool(key: String, fallback: Boolean): Boolean
    fun putBool(key: String, value: Boolean)
}

class PrefsKeyValue(private val prefs: SharedPreferences) : KeyValue {
    constructor(context: Context) : this(context.getSharedPreferences("BeebLocal", Context.MODE_PRIVATE))
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String?) { prefs.edit().apply { if (value == null) remove(key) else this.putString(key, value) }.apply() }
    override fun getBool(key: String, fallback: Boolean) = prefs.getBoolean(key, fallback)
    override fun putBool(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
}

class MapKeyValue : KeyValue {
    val values = mutableMapOf<String, Any>()
    override fun getString(key: String) = values[key] as? String
    override fun putString(key: String, value: String?) { if (value == null) values.remove(key) else values[key] = value }
    override fun getBool(key: String, fallback: Boolean) = values[key] as? Boolean ?: fallback
    override fun putBool(key: String, value: Boolean) { values[key] = value }
}
