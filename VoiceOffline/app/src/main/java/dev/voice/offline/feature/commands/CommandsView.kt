package dev.voice.offline.feature.commands

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class CommandsView(context: Context) : ScrollView(context) {

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
            text = "Supported Commands"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 8)
        }
        root.addView(header)

        val subheader = TextView(context).apply {
            text = "Voice supports 13 native Android mobile actions powered by FunctionGemma 270M. Say 'Hey Voice' followed by:"
            textSize = 13f
            setTextColor(Color.parseColor("#9E9E9E"))
            setPadding(0, 0, 0, 36)
        }
        root.addView(subheader)

        fun addCommandCard(actionName: String, examples: List<String>, description: String, securityNote: String = "") {
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

            val title = TextView(context).apply {
                text = actionName
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#64B5F6"))
            }
            card.addView(title)

            val desc = TextView(context).apply {
                text = description
                textSize = 12f
                setTextColor(Color.parseColor("#A0A0A0"))
                setPadding(0, 4, 0, 12)
            }
            card.addView(desc)

            val exampleContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 16, 20, 16)
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#15161C"))
                    cornerRadius = 12f
                }
            }

            for (ex in examples) {
                val exView = TextView(context).apply {
                    text = "• \"$ex\""
                    textSize = 13f
                    setTypeface(Typeface.MONOSPACE)
                    setTextColor(Color.WHITE)
                    setPadding(0, 2, 0, 2)
                }
                exampleContainer.addView(exView)
            }
            card.addView(exampleContainer)

            if (securityNote.isNotBlank()) {
                val sec = TextView(context).apply {
                    text = "🔒 $securityNote"
                    textSize = 11f
                    setTextColor(Color.parseColor("#FFA726"))
                    setPadding(0, 8, 0, 0)
                }
                card.addView(sec)
            }

            root.addView(card)
        }

        addCommandCard("make_call", listOf("call Amma", "phone Rahul", "call Alice on mobile"), "Resolves contact in phone book and places a direct phone call.", "User confirmation required for calls")
        addCommandCard("answer_call", listOf("answer call", "answer the phone", "pick up"), "Answers an incoming ringing phone call.")
        addCommandCard("reject_call", listOf("reject call", "decline call", "hang up"), "Declines the incoming ringing phone call.")
        addCommandCard("open_camera", listOf("open camera", "launch camera", "take a picture"), "Launches the native camera application.")
        addCommandCard("lock_screen", listOf("lock screen", "lock my phone"), "Immediately locks the phone screen using Device Policy Manager.")
        addCommandCard("take_notes", listOf("take a note buy milk", "remember to pick up groceries", "note down meeting at 3pm"), "Stores an AES-256-GCM encrypted note locally in SQLite.")
        addCommandCard("open_app", listOf("open YouTube", "launch Maps", "open Calculator"), "Launches any installed application by name or package.")
        addCommandCard("set_alarm", listOf("set alarm for 7:30 AM", "set an alarm at 8 AM labeled workout"), "Creates a clock alarm on the device.")
        addCommandCard("turn_flashlight_on", listOf("turn on flashlight", "flashlight on", "torch on"), "Activates the device camera LED torch.")
        addCommandCard("turn_flashlight_off", listOf("turn off flashlight", "flashlight off", "torch off"), "Deactivates the device camera LED torch.")
        addCommandCard("text_msg", listOf("text Amma saying I will be home soon", "message Rahul saying see you tomorrow"), "Drafts or sends a text message to a contact.", "Biometric authentication required")
        addCommandCard("read_notifications", listOf("read notifications", "what are my notifications"), "Reads recent unread notifications aloud.")
        addCommandCard("reply_to_notification", listOf("reply saying on my way", "respond to WhatsApp saying yes"), "Sends a direct reply to an active notification via RemoteInput.", "Biometric authentication required")
    }
}
