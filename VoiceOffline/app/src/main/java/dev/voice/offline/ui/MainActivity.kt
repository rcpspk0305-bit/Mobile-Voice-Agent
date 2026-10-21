package dev.voice.offline.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dev.voice.offline.VoiceAssistantService
import dev.voice.offline.feature.about.AboutView
import dev.voice.offline.feature.commands.CommandsView
import dev.voice.offline.feature.home.HomeView
import dev.voice.offline.feature.notes.NotesView
import dev.voice.offline.feature.onboarding.OnboardingView
import dev.voice.offline.feature.permissions.PermissionsView
import dev.voice.offline.feature.security.SecurityView
import dev.voice.offline.feature.settings.SettingsView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private lateinit var contentContainer: FrameLayout
    private lateinit var tabContainer: LinearLayout

    private lateinit var homeView: HomeView
    private lateinit var commandsView: CommandsView
    private lateinit var permissionsView: PermissionsView
    private lateinit var securityView: SecurityView
    private lateinit var notesView: NotesView
    private lateinit var settingsView: SettingsView
    private lateinit var aboutView: AboutView
    private lateinit var onboardingView: OnboardingView

    private var currentTabTag: String = TAB_HOME
    private val tabButtons = mutableMapOf<String, TextView>()

    companion object {
        const val TAB_HOME = "home"
        const val TAB_COMMANDS = "commands"
        const val TAB_PERMISSIONS = "permissions"
        const val TAB_SECURITY = "security"
        const val TAB_NOTES = "notes"
        const val TAB_SETTINGS = "settings"
        const val TAB_ABOUT = "about"
        const val TAB_ONBOARDING = "onboarding"

        private const val REQUEST_AUDIO_PERMISSION = 100
        private const val PREFS_NAME = "voice_app_prefs"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root layout (dark mode by default)
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121216"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(rootLayout)

        // Top Horizontal Tab Bar
        val tabScrollView = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(Color.parseColor("#1A1A22"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        tabContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }
        tabScrollView.addView(tabContainer)
        rootLayout.addView(tabScrollView)

        // Content Area
        contentContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        rootLayout.addView(contentContainer)

        // Initialize All 8 Views
        homeView = HomeView(
            context = this,
            onStartListening = { handleStartListening() },
            onStopListening = { handleStopListening() },
            onNavigate = { screenTag -> selectTab(screenTag) }
        )
        commandsView = CommandsView(this)
        permissionsView = PermissionsView(this) { permissions, requestCode ->
            requestPermissions(permissions, requestCode)
        }
        securityView = SecurityView(this)
        notesView = NotesView(this)
        settingsView = SettingsView(this)
        aboutView = AboutView(this)
        onboardingView = OnboardingView(this) {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ONBOARDING_DONE, true)
                .apply()
            selectTab(TAB_HOME)
        }

        // Setup Tabs
        setupTab(TAB_HOME, "Home")
        setupTab(TAB_COMMANDS, "Commands")
        setupTab(TAB_PERMISSIONS, "Permissions")
        setupTab(TAB_SECURITY, "Security")
        setupTab(TAB_NOTES, "Notes")
        setupTab(TAB_SETTINGS, "Settings")
        setupTab(TAB_ABOUT, "About")

        // Check if first run
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        if (!onboardingDone) {
            selectTab(TAB_ONBOARDING)
        } else {
            selectTab(TAB_HOME)
        }

        // Observe Assistant State and Last Action Result
        activityScope.launch {
            VoiceAssistantService.stateFlow.collect { state ->
                homeView.updateState(state)
            }
        }
        activityScope.launch {
            VoiceAssistantService.lastResultFlow.collect { result ->
                homeView.updateLastResult(result)
            }
        }
    }

    private fun setupTab(tag: String, label: String) {
        val tabItem = TextView(this).apply {
            text = label
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#9E9E9E"))
            setPadding(28, 14, 28, 14)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                cornerRadius = 20f
            }
            setOnClickListener { selectTab(tag) }
        }
        tabButtons[tag] = tabItem
        tabContainer.addView(tabItem)
    }

    private fun selectTab(tag: String) {
        currentTabTag = tag
        contentContainer.removeAllViews()

        val activeView: View = when (tag) {
            TAB_HOME -> homeView
            TAB_COMMANDS -> commandsView
            TAB_PERMISSIONS -> permissionsView
            TAB_SECURITY -> securityView
            TAB_NOTES -> {
                notesView.loadNotes()
                notesView
            }
            TAB_SETTINGS -> settingsView
            TAB_ABOUT -> aboutView
            TAB_ONBOARDING -> onboardingView
            else -> homeView
        }
        contentContainer.addView(activeView)

        // Update Tab Button Styles
        tabButtons.forEach { (tabTag, textView) ->
            if (tabTag == tag) {
                textView.setTextColor(Color.parseColor("#FFFFFF"))
                textView.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#3F51B5"))
                    cornerRadius = 20f
                }
            } else {
                textView.setTextColor(Color.parseColor("#8E8E93"))
                textView.background = GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                    cornerRadius = 20f
                }
            }
        }
    }

    private fun handleStartListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_AUDIO_PERMISSION)
        } else {
            try {
                startForegroundService(
                    Intent(this, VoiceAssistantService::class.java)
                        .setAction(VoiceAssistantService.ACTION_START)
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Cannot start microphone service: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleStopListening() {
        startService(
            Intent(this, VoiceAssistantService::class.java)
                .setAction(VoiceAssistantService.ACTION_STOP)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            handleStartListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}
