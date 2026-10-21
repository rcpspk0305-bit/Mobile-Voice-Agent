package dev.voice.offline.service

/**
 * Observable runtime states for Voice assistant lifecycle and pipeline processing.
 */
sealed interface AssistantState {
    data object STOPPED : AssistantState
    data object INITIALIZING : AssistantState
    data object LISTENING : AssistantState
    data object WAKE_DETECTED : AssistantState
    data object RECORDING_COMMAND : AssistantState
    data object TRANSCRIBING : AssistantState
    data object INFERENCING : AssistantState
    data object AUTHORIZING : AssistantState
    data object EXECUTING : AssistantState
    data object CONFIRMING : AssistantState
    data class ERROR(val message: String) : AssistantState
}
