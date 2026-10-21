package dev.voice.offline.feature.home

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import dev.voice.offline.service.AssistantState

class HomeView(
    context: Context,
    private val onStartListening: () -> Unit,
    private val onStopListening: () -> Unit,
    private val onNavigate: (String) -> Unit
) : ScrollView(context) {

    private val statusTextView: TextView
    private val actionButton: Button
    private val lastResultCard: LinearLayout
    private val lastResultTextView: TextView

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        isFillViewport = true

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        addView(root)

        // Brand Title
        val title = TextView(context).apply {
            text = "Voice"
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title)

        // Subtitle / Positioning
        val subtitle = TextView(context).apply {
            text = "Your phone. Your voice. Your control."
            textSize = 14f
            setTextColor(Color.parseColor("#9E9E9E"))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 48)
        }
        root.addView(subtitle)

        // Status Card
        val statusCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1E1E24"))
                cornerRadius = 24f
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        root.addView(statusCard)

        val statusLabel = TextView(context).apply {
            text = "ASSISTANT STATUS"
            textSize = 11f
            setTextColor(Color.parseColor("#8E8E93"))
            letterSpacing = 0.1f
        }
        statusCard.addView(statusLabel)

        statusTextView = TextView(context).apply {
            text = "STOPPED"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF5252"))
            setPadding(0, 12, 0, 0)
        }
        statusCard.addView(statusTextView)

        // Spacer
        root.addView(View(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 48)
        })

        // Prominent Start/Stop Action Button
        actionButton = Button(context).apply {
            text = "Start Offline Listening"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#64B5F6"))
                cornerRadius = 40f
            }
            setPadding(32, 28, 32, 28)
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                if (text == "Stop Listening") onStopListening()
                else onStartListening()
            }
        }
        root.addView(actionButton)

        // Last Result Card
        lastResultCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#252836"))
                cornerRadius = 20f
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 36, 0, 0)
            }
            visibility = View.GONE
        }
        root.addView(lastResultCard)

        val resultHeader = TextView(context).apply {
            text = "LAST ACTION RESULT"
            textSize = 11f
            setTextColor(Color.parseColor("#8E8E93"))
            letterSpacing = 0.1f
        }
        lastResultCard.addView(resultHeader)

        lastResultTextView = TextView(context).apply {
            textSize = 15f
            setTextColor(Color.parseColor("#E0E0E0"))
            setPadding(0, 8, 0, 0)
        }
        lastResultCard.addView(lastResultTextView)

        // Wake Phrase Hint
        val hintText = TextView(context).apply {
            text = "Wake phrase: “Hey Voice” or “Okay Voice”\nAll AI models run locally on this phone."
            textSize = 13f
            setTextColor(Color.parseColor("#757575"))
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 16)
        }
        root.addView(hintText)
    }

    fun updateState(state: AssistantState) {
        when (state) {
            AssistantState.STOPPED -> {
                statusTextView.text = "STOPPED"
                statusTextView.setTextColor(Color.parseColor("#FF5252"))
                actionButton.text = "Start Offline Listening"
                (actionButton.background as? GradientDrawable)?.setColor(Color.parseColor("#64B5F6"))
            }
            AssistantState.INITIALIZING -> {
                statusTextView.text = "INITIALIZING..."
                statusTextView.setTextColor(Color.parseColor("#FFD54F"))
                actionButton.text = "Stop Listening"
                (actionButton.background as? GradientDrawable)?.setColor(Color.parseColor("#EF5350"))
            }
            AssistantState.LISTENING -> {
                statusTextView.text = "LISTENING"
                statusTextView.setTextColor(Color.parseColor("#66BB6A"))
                actionButton.text = "Stop Listening"
                (actionButton.background as? GradientDrawable)?.setColor(Color.parseColor("#EF5350"))
            }
            AssistantState.WAKE_DETECTED -> {
                statusTextView.text = "WAKE DETECTED"
                statusTextView.setTextColor(Color.parseColor("#42A5F5"))
            }
            AssistantState.RECORDING_COMMAND,
            AssistantState.TRANSCRIBING -> {
                statusTextView.text = "RECORDING SPEECH..."
                statusTextView.setTextColor(Color.parseColor("#AB47BC"))
            }
            AssistantState.INFERENCING -> {
                statusTextView.text = "FUNCTIONGEMMA INFERENCE..."
                statusTextView.setTextColor(Color.parseColor("#26A69A"))
            }
            AssistantState.AUTHORIZING -> {
                statusTextView.text = "AWAITING BIOMETRICS..."
                statusTextView.setTextColor(Color.parseColor("#FFA726"))
            }
            AssistantState.EXECUTING -> {
                statusTextView.text = "EXECUTING ACTION..."
                statusTextView.setTextColor(Color.parseColor("#42A5F5"))
            }
            AssistantState.CONFIRMING -> {
                statusTextView.text = "CONFIRMED"
                statusTextView.setTextColor(Color.parseColor("#66BB6A"))
            }
            is AssistantState.ERROR -> {
                statusTextView.text = "ERROR: ${state.message}"
                statusTextView.setTextColor(Color.parseColor("#FF5252"))
                actionButton.text = "Start Offline Listening"
                (actionButton.background as? GradientDrawable)?.setColor(Color.parseColor("#64B5F6"))
            }
        }
    }

    fun updateLastResult(result: String?) {
        if (result.isNullOrBlank()) {
            lastResultCard.visibility = View.GONE
        } else {
            lastResultCard.visibility = View.VISIBLE
            lastResultTextView.text = result
        }
    }
}
