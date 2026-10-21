package dev.voice.offline.feature.security

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import dev.voice.offline.security.BiometricAuthorizationManager
import dev.voice.offline.security.BiometricReadiness
import dev.voice.offline.security.HardwareSecurityTier
import dev.voice.offline.security.KeystoreManager

class SecurityView(context: Context) : ScrollView(context) {

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
            text = "Security & Biometrics"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 8)
        }
        root.addView(header)

        val subheader = TextView(context).apply {
            text = "Hardware security, biometric authentication policies, and cryptographic verification."
            textSize = 13f
            setTextColor(Color.parseColor("#9E9E9E"))
            setPadding(0, 0, 0, 36)
        }
        root.addView(subheader)

        fun addStatusCard(title: String, value: String, subtitle: String, isOk: Boolean) {
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
                textSize = 12f
                setTextColor(Color.parseColor("#8E8E93"))
                letterSpacing = 0.08f
            }
            card.addView(titleView)

            val valView = TextView(context).apply {
                text = value
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (isOk) Color.parseColor("#66BB6A") else Color.parseColor("#FFA726"))
                setPadding(0, 4, 0, 4)
            }
            card.addView(valView)

            val subView = TextView(context).apply {
                text = subtitle
                textSize = 12f
                setTextColor(Color.parseColor("#9E9E9E"))
            }
            card.addView(subView)

            root.addView(card)
        }

        val bioManager = BiometricAuthorizationManager(context)
        val readiness = bioManager.checkBiometricReadiness()
        val hwTier = KeystoreManager.getHardwareSecurityTier()

        addStatusCard(
            "BIOMETRIC ENROLLMENT",
            readiness.name,
            "Used to authenticate sensitive voice commands prior to execution.",
            readiness == BiometricReadiness.READY
        )

        addStatusCard(
            "HARDWARE SECURITY TIER",
            hwTier.name,
            "Audited device cryptographic hardware backing for Android Keystore.",
            hwTier == HardwareSecurityTier.STRONGBOX || hwTier == HardwareSecurityTier.TRUSTED_ENVIRONMENT
        )

        addStatusCard(
            "KEYSTORE CRYPTOGRAPHY",
            "AES-256-GCM + EC P-256",
            "Notes encrypted locally with hardware-backed AES keys; signatures invalidated on biometric change.",
            true
        )

        addStatusCard(
            "COMMAND TTL TIMEOUT",
            "30 SECONDS",
            "Pending high-risk voice commands expire automatically if unapproved to prevent replay attacks.",
            true
        )
    }
}
