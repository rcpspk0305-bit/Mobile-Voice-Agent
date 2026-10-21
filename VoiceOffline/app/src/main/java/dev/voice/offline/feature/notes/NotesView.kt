package dev.voice.offline.feature.notes

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
import dev.voice.offline.storage.PrivateNoteStore

class NotesView(context: Context) : ScrollView(context) {

    private val notesContainer: LinearLayout

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
            text = "Encrypted Local Notes"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 8)
        }
        root.addView(header)

        val subheader = TextView(context).apply {
            text = "Notes created via 'take a note' are encrypted with Android Keystore AES-256-GCM and stored only on your device."
            textSize = 13f
            setTextColor(Color.parseColor("#9E9E9E"))
            setPadding(0, 0, 0, 24)
        }
        root.addView(subheader)

        val refreshBtn = Button(context).apply {
            text = "Refresh Notes"
            textSize = 13f
            setTextColor(Color.BLACK)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#64B5F6"))
                cornerRadius = 24f
            }
            isAllCaps = false
            setOnClickListener { loadNotes() }
        }
        root.addView(refreshBtn)

        notesContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 24, 0, 0)
        }
        root.addView(notesContainer)

        loadNotes()
    }

    fun loadNotes() {
        notesContainer.removeAllViews()

        val notes = try {
            PrivateNoteStore(context).listRecent(50)
        } catch (_: Exception) {
            emptyList()
        }

        if (notes.isEmpty()) {
            val emptyView = TextView(context).apply {
                text = "No notes saved yet.\nSay “Hey Voice, take a note buy milk” to save a note."
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
                gravity = Gravity.CENTER
                setPadding(0, 48, 0, 48)
            }
            notesContainer.addView(emptyView)
            return
        }

        for ((idx, noteText) in notes.withIndex()) {
            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(28, 20, 28, 20)
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1E1E24"))
                    cornerRadius = 16f
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            val numView = TextView(context).apply {
                text = "NOTE #${idx + 1}"
                textSize = 10f
                setTextColor(Color.parseColor("#8E8E93"))
                letterSpacing = 0.1f
            }
            card.addView(numView)

            val contentView = TextView(context).apply {
                text = noteText
                textSize = 15f
                setTextColor(Color.WHITE)
                setPadding(0, 6, 0, 0)
            }
            card.addView(contentView)

            notesContainer.addView(card)
        }
    }
}
