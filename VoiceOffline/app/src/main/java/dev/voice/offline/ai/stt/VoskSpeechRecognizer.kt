package dev.voice.offline.ai.stt

import android.os.SystemClock
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer

class VoskSpeechRecognizer(
    model: Model
) : OfflineSpeechRecognizer {

    private val recognizer = Recognizer(model, 16000f)
    private var maxDurationDeadline = 0L
    private var silenceDeadline = 0L
    private var hasSpoken = false

    override fun startListening(maxDurationMs: Long, silenceTimeoutMs: Long) {
        recognizer.reset()
        val now = currentTime()
        maxDurationDeadline = now + maxDurationMs
        silenceDeadline = now + silenceTimeoutMs
        hasSpoken = false
    }

    override fun offer(buffer: ShortArray, count: Int): SpeechRecognitionResult {
        val now = currentTime()

        if (recognizer.acceptWaveForm(buffer, count)) {
            val text = parseResultText(recognizer.result)
            if (text.isNotBlank()) {
                return SpeechRecognitionResult.Final(text)
            }
        } else {
            val partial = parsePartialText(recognizer.partialResult)
            if (partial.isNotBlank()) {
                hasSpoken = true
                silenceDeadline = now + 2500L // Reset silence timer on active speech
                return SpeechRecognitionResult.Partial(partial)
            }
        }

        if (now >= maxDurationDeadline) {
            val text = finish()
            return if (text.isNotBlank()) SpeechRecognitionResult.Final(text)
            else SpeechRecognitionResult.MaxDurationExceeded
        }

        if (hasSpoken && now >= silenceDeadline) {
            val text = finish()
            return if (text.isNotBlank()) SpeechRecognitionResult.Final(text)
            else SpeechRecognitionResult.SilenceTimeout
        }

        return SpeechRecognitionResult.Listening
    }

    override fun finish(): String {
        val text = parseResultText(recognizer.finalResult)
        reset()
        return text
    }

    override fun reset() {
        recognizer.reset()
        maxDurationDeadline = 0L
        silenceDeadline = 0L
        hasSpoken = false
    }

    override fun close() {
        recognizer.close()
    }

    private fun parseResultText(json: String): String {
        return try {
            JSONObject(json).optString("text").trim()
        } catch (_: Exception) {
            ""
        }
    }

    private fun parsePartialText(json: String): String {
        return try {
            JSONObject(json).optString("partial").trim()
        } catch (_: Exception) {
            ""
        }
    }

    private fun currentTime(): Long {
        return try {
            SystemClock.elapsedRealtime()
        } catch (_: Throwable) {
            System.currentTimeMillis()
        }
    }
}
