package dev.voice.offline.feature.permissions

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.voice.offline.system.ReplyNotificationListener
import dev.voice.offline.system.VoiceAdminReceiver

class PermissionsView(
    context: Context,
    private val onRequestPermission: (Array<String>, Int) -> Unit
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
            text = "Permissions & Access"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 8)
        }
        root.addView(header)

        val subheader = TextView(context).apply {
            text = "Voice requires explicit permissions to interact with your phone offline. No permission is granted silently."
            textSize = 13f
            setTextColor(Color.parseColor("#9E9E9E"))
            setPadding(0, 0, 0, 36)
        }
        root.addView(subheader)

        fun addPermissionRow(
            name: String,
            description: String,
            isGranted: Boolean,
            actionLabel: String,
            onAction: () -> Unit
        ) {
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

            val topRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            card.addView(topRow)

            val titleView = TextView(context).apply {
                text = name
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            topRow.addView(titleView)

            val statusBadge = TextView(context).apply {
                text = if (isGranted) "GRANTED" else "REQUIRED"
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (isGranted) Color.parseColor("#66BB6A") else Color.parseColor("#FFA726"))
            }
            topRow.addView(statusBadge)

            val descView = TextView(context).apply {
                text = description
                textSize = 12f
                setTextColor(Color.parseColor("#9E9E9E"))
                setPadding(0, 8, 0, 16)
            }
            card.addView(descView)

            if (!isGranted) {
                val btn = Button(context).apply {
                    text = actionLabel
                    textSize = 13f
                    setTextColor(Color.BLACK)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#64B5F6"))
                        cornerRadius = 24f
                    }
                    isAllCaps = false
                    setOnClickListener { onAction() }
                }
                card.addView(btn)
            }

            root.addView(card)
        }

        // Microphone
        val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        addPermissionRow("Microphone", "Needed to capture voice commands and detect the wake phrase offline.", micGranted, "Grant Microphone") {
            onRequestPermission(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
        }

        // Notifications
        val notifGranted = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        addPermissionRow("Notifications", "Displays listening status and secure biometric action approval prompts.", notifGranted, "Grant Notifications") {
            if (Build.VERSION.SDK_INT >= 33) {
                onRequestPermission(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Contacts & Phone
        val contactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val phoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        val callAnswerGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED
        val telephonyGranted = contactsGranted && phoneGranted && callAnswerGranted

        addPermissionRow("Phone & Contacts", "Allows searching contacts by name to initiate calls or answer incoming calls.", telephonyGranted, "Grant Phone & Contacts") {
            onRequestPermission(arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ANSWER_PHONE_CALLS
            ), 102)
        }

        // Device Admin (Screen Locking)
        val dpm = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, VoiceAdminReceiver::class.java)
        val adminActive = dpm?.isAdminActive(admin) == true
        addPermissionRow("Device Administrator", "Allows Voice to lock the screen when you say 'lock screen'.", adminActive, "Enable Screen Lock Admin") {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Voice only uses this permission to lock your screen upon voice command.")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        // Notification Listener (Replies)
        val listenerActive = ReplyNotificationListener.isConnected()
        addPermissionRow("Notification Listener", "Required to read incoming messages and perform direct replies via RemoteInput.", listenerActive, "Enable Notification Access") {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
