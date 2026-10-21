package dev.voice.offline.audio

/** Swappable offline recognizer boundary for dedicated KWS + STT or acoustic classifier. */
interface SpeechPipeline : AutoCloseable {
    fun offer(buffer: ShortArray, count: Int)
}
