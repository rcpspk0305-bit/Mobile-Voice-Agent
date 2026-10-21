package dev.voice.offline.commands

import org.junit.Assert.*
import org.junit.Test

class IntentParserTest {
    private val parser = IntentParser()
    @Test fun parsesSupportedActions() {
        assertEquals(VoiceCommand.LockScreen, parser.parse("lock my phone"))
        assertEquals(VoiceCommand.Call("alice"), parser.parse("Call Alice"))
        assertEquals(VoiceCommand.Reply("alice", "hello there"),
            parser.parse("reply to alice saying hello there"))
        assertEquals(VoiceCommand.Note("buy milk"), parser.parse("take a note buy milk"))
    }
    @Test fun rejectsUnsupportedOrAmbiguousCommands() {
        assertNull(parser.parse("transfer money to bob"))
        assertNull(parser.parse("call"))
        assertNull(parser.parse("text mom hello"))
        assertNull(parser.parse("open my banking app"))
    }
}
