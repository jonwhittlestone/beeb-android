// SPDX-License-Identifier: GPL-3.0-or-later
package org.beebplayer.android

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import android.widget.TextView

/** Shared TV view helpers. Extensions on Context, so any screen or dialog can build the same controls. */
object Palette {
    val pink = Color.rgb(250, 35, 137)
    val ink = Color.rgb(16, 16, 22)
}

fun Context.dp(value: Int) = (value * resources.displayMetrics.density).toInt()

fun Context.label(text: String, size: Float = 22f, muted: Boolean = false) = TextView(this).apply {
    this.text = text; textSize = size; setTextColor(if (muted) 0xFFB6B2C0.toInt() else Color.WHITE); setPadding(0, dp(6), 0, dp(8))
}

fun Context.border(focused: Boolean) = GradientDrawable().apply {
    setColor(if (focused) 0xFF382039.toInt() else 0xFF1C1B24.toInt()); cornerRadius = dp(10).toFloat()
    setStroke(dp(if (focused) 3 else 1), if (focused) Palette.pink else 0xFF47434E.toInt())
}

fun Context.focus(view: View) {
    view.isFocusable = true; view.isFocusableInTouchMode = true; view.background = border(false)
    view.setOnFocusChangeListener { v, on -> v.background = border(on) }
}

fun Context.button(text: String, action: () -> Unit) = Button(this).apply {
    this.text = text; isAllCaps = false; textSize = 18f; setTextColor(Color.WHITE); setPadding(dp(20), dp(8), dp(20), dp(8))
    focus(this); setOnClickListener { action() }
}
