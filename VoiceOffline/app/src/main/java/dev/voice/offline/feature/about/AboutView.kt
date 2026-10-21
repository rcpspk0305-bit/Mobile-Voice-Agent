package dev.voice.offline.feature.about

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class AboutView(context: Context) : ScrollView(context) {

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
            text = "About & Privacy"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 8)
        }
        root.addView(header)

        val subheader = TextView(context).apply {
            text = "Voice is an air-gapped on-device assistant built for privacy, performance, and complete user agency."
            textSize = 13f
            setTextColor(Color.parseColor("#9E9E9E"))
            setPadding(0, 0, 0, 36)
        }
        root.addView(subheader)

        fun addInfoSection(title: String, content: String) {
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
                setTextColor(Color.parseColor("#64B5F6"))
            }
            card.addView(titleView)

            val contentView = TextView(context).apply {
                text = content
                textSize = 13f
                setTextColor(Color.parseColor("#C0C0C0"))
                setPadding(0, 8, 0, 0)
                setLineSpacing(4f, 1f)
            }
            card.addView(contentView)

            root.addView(card)
        }

        addInfoSection(
            "Air-Gapped Offline Guarantee",
            "The offline build of Voice contains NO android.permission.INTERNET in its AndroidManifest. It cannot connect to any remote server, cloud API, telemetry service, or analytics platform. Test in Airplane Mode."
        )

        addInfoSection(
            "On-Device AI Models",
            "• Intent Model: FunctionGemma 270M Mobile Actions (litert-community/functiongemma-270m-ft-mobile-actions)\n" +
            "• Runtime: Google LiteRT-LM (.litertlm quantized)\n" +
            "• Speech Recognition: Vosk Offline STT with Indian English phonetic support"
        )

        addInfoSection(
            "Microphone & Audio Privacy",
            "Raw PCM microphone audio is streamed strictly to volatile RAM for wake-word and command transcription, and immediately discarded. Voice does not record, log, or persist audio files."
        )

        addInfoSection(
            "Local Data Encryption",
            "All notes are stored in local SQLite encrypted with AES-256-GCM under Android Keystore. Sensitive actions require BiometricPrompt with cryptographic signature verification."
        )

        addInfoSection(
            "Product Version",
            "Voice v0.1.0 (Mobile Actions Edition)\nPackage: dev.voice.offline\nCompile SDK: 36 (Android 16 Ready)"
        )
    }
}
