package dev.voice.offline.security

import dev.voice.offline.ai.functioncalling.StructuredAction
import java.util.concurrent.ConcurrentHashMap

enum class ActionRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Classifies actions into risk tiers with configurable overrides.
 * High risk actions enforce biometric authorization before execution.
 */
class SensitiveActionPolicy {

    private val overrides = ConcurrentHashMap<String, ActionRiskLevel>()

    fun setOverride(functionName: String, level: ActionRiskLevel) {
        overrides[functionName] = level
    }

    fun getRiskLevel(action: StructuredAction): ActionRiskLevel {
        overrides[action.functionName]?.let { return it }

        return when (action) {
            is StructuredAction.OpenCamera,
            is StructuredAction.TurnFlashlightOn,
            is StructuredAction.TurnFlashlightOff,
            is StructuredAction.TakeNotes,
            is StructuredAction.OpenApp,
            is StructuredAction.LockScreen -> ActionRiskLevel.LOW

            is StructuredAction.MakeCall,
            is StructuredAction.SetAlarm,
            is StructuredAction.ReadNotifications,
            is StructuredAction.AnswerCall,
            is StructuredAction.RejectCall -> ActionRiskLevel.MEDIUM

            is StructuredAction.TextMsg,
            is StructuredAction.ReplyToNotification -> ActionRiskLevel.HIGH
        }
    }

    fun requiresBiometrics(action: StructuredAction): Boolean {
        return getRiskLevel(action) == ActionRiskLevel.HIGH
    }
}
