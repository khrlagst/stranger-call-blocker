// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumberRulesTest {

    @Test
    fun `phone shape accepts real numbers and rejects labels`() {
        assertTrue(NumberRules.isPhoneNumberShape("+6285592679948"))
        assertTrue(NumberRules.isPhoneNumberShape("1234567"))
        assertFalse(NumberRules.isPhoneNumberShape("WhatsApp Call"))
        assertFalse(NumberRules.isPhoneNumberShape("123"))
        assertFalse(NumberRules.isPhoneNumberShape("1234567890123456")) // 16 digits
    }

    @Test
    fun `normalize strips formatting but keeps alpha senders`() {
        assertEquals("628123456", NumberRules.normalizeSender("+62 812-3456"))
        assertEquals("TELKOMSEL", NumberRules.normalizeSender("TELKOMSEL"))
        assertEquals("6285592679948", NumberRules.normalizeSender("+62 855 9267 9948"))
    }

    @Test
    fun `inRange handles open and closed bounds`() {
        val t = 1_000_000L
        assertTrue(NumberRules.inRange(t, null, null))
        assertTrue(NumberRules.inRange(t, 500_000L, 1_500_000L))
        assertFalse(NumberRules.inRange(t, 1_500_000L, null))
        assertFalse(NumberRules.inRange(t, null, 500_000L))
        assertTrue(NumberRules.inRange(t, t, t)) // inclusive bounds
    }
}
