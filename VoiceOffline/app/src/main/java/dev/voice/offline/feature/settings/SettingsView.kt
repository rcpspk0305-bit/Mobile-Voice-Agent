package dev.voice.offline.feature.settings

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class SettingsView(context: Context) : ScrollView(context) {

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        isFillViewport = true

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }
        addView(root)

        val header = TextView(context).apply {
            text = "Voice Settings"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 8)
        }
        root.addView(header)

        val subheader = TextView(context).apply {
            text = "Configure acoustic parameters, wake phrase sensitivity, and audio chime feedback."
            textSize = 13f
            setTextColor(Color.parseColor("#9E9E9E"))
            setPadding(0, 0, 0, 36)
        }
        root.addView(subheader)

        fun addSettingItem(title: String, currentVal: String, description: String) {
            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 24, 32, 24)
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1E1E24"))
                    cornerRadius = 20f
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 20)
                }
            }

            val titleView = TextView(context).apply {
                text = title
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
            }
            card.addView(titleView)

            val valView = TextView(context).apply {
                text = currentVal
                textSize = 13f
                setTextColor(Color.parseColor("#64B5F6"))
                setPadding(0, 4, 0, 4)
            }
            card.addView(valView)

            val descView = TextView(context).apply {
                text = description
                textSize = 12f
                setTextColor(Color.parseColor("#9E9E9E"))
            }
            card.addView(descView)

            root.addView(card)
        }

        addSettingItem("Wake Phrase", "“Hey Voice” / “Okay Voice”", "Lightweight offline keyword spotting powered by Vosk acoustic model.")
        addSettingItem("Recognition Language", "English (Indian English & US English)", "Phonetic model tuned for natural regional pronunciations.")
        addSettingItem("Listening Sensitivity", "Standard (Cooldown 2.0s)", "Prevents false-positive duplicate triggers.")
        addSettingItem("Silence Timeout", "3.0 Seconds", "Automatically finishes recording command after user stops speaking.")
        addSettingItem("Audio Feedback Chime", "Enabled", "Plays a short confirmation chime when wake word or action completes.")
    }
}
