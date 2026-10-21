package dev.voice.offline.feature.onboarding

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class OnboardingView(
    context: Context,
    private val onGetStarted: () -> Unit
) : ScrollView(context) {

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
            text = "Welcome to Voice"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        }
        root.addView(header)

        val subheader = TextView(context).apply {
            text = "Your device. Your voice. Fully offline."
            textSize = 15f
            setTextColor(Color.parseColor("#9E9E9E"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }
        root.addView(subheader)

        fun addFeatureCard(title: String, description: String, iconTag: String) {
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
                    setMargins(0, 0, 0, 24)
                }
            }

            val cardTitle = TextView(context).apply {
                text = "$iconTag  $title"
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
            }
            card.addView(cardTitle)

            val cardDesc = TextView(context).apply {
                text = description
                textSize = 13f
                setTextColor(Color.parseColor("#B0B0B0"))
                setPadding(0, 8, 0, 0)
                setLineSpacing(4f, 1f)
            }
            card.addView(cardDesc)

            root.addView(card)
        }

        addFeatureCard("100% On-Device AI", "Speech recognition and FunctionGemma 270M Mobile Actions run entirely on your phone. No internet access, no remote servers, zero cloud telemetry.", "🔒")
        addFeatureCard("Actions, Not Chatbot", "Designed for action execution: make calls, send texts, lock your screen, take encrypted notes, set alarms, and control your flashlight without conversational fluff.", "⚡")
        addFeatureCard("Biometric Security", "High-risk actions like sending messages and replying to notifications require explicit biometric confirmation with Android Keystore hardware protection.", "🛡️")
        addFeatureCard("User Controlled", "The microphone only listens when you explicitly turn it on. A persistent notification with an instant Stop button keeps you in full control at all times.", "🎙️")

        val startBtn = Button(context).apply {
            text = "Get Started"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#64B5F6"))
                cornerRadius = 32f
            }
            setPadding(32, 24, 32, 24)
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 24, 0, 16)
            }
            setOnClickListener { onGetStarted() }
        }
        root.addView(startBtn)
    }
}
