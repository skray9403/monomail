package com.shrivatsav.monomail.dockdemo

import org.junit.Assert.assertEquals
import org.junit.Test

class ReplyTargetResolverTest {
    @Test
    fun latestOutgoingMessageDoesNotMakeReplyTargetTheCurrentUser() {
        val messages = listOf(
            DemoMessage("Alex", "alex@example.com", "me@monomail.demo", "9:00", 1L, "Incoming"),
            DemoMessage("You", "me@monomail.demo", "alex@example.com", "9:30", 2L, "Outgoing")
        )

        assertEquals("alex@example.com", resolveReplyTarget(messages, "me@monomail.demo"))
    }

    @Test
    fun allOutgoingConversationHasNoReplyTargetRatherThanReturningSelf() {
        val messages = listOf(
            DemoMessage("You", "me@monomail.demo", "alex@example.com", "9:30", 2L, "Outgoing")
        )

        assertEquals(null, resolveReplyTarget(messages, "me@monomail.demo"))
    }
}
