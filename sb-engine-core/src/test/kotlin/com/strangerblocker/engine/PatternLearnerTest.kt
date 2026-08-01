// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PatternLearnerTest {

    @Test
    fun `learns a pattern from a mobile spam range`() {
        val numbers = listOf(
            "+6285592679948",
            "+6285592679125",
            "+6285592679231",
        )
        val patterns = PatternLearner.learn(numbers)
        assertEquals(1, patterns.size)
        assertEquals("+6285592679", patterns[0].prefix)
        assertEquals(3, patterns[0].count)
    }

    @Test
    fun `splits a landline range by threshold - shorter neighbor excluded`() {
        val numbers = listOf(
            "+622130179723",
            "+622130183252",
            "+622130911574", // shares only 7 chars — below the 8-char threshold
        )
        val patterns = PatternLearner.learn(numbers)
        assertEquals(1, patterns.size)
        assertEquals("+6221301", patterns[0].prefix)
        assertEquals(2, patterns[0].count)
    }

    @Test
    fun `finds a second tighter cluster in the same range`() {
        val numbers = listOf(
            "+622135523726",
            "+622135523755",
        )
        val patterns = PatternLearner.learn(numbers)
        assertEquals(1, patterns.size)
        assertEquals("+6221355237", patterns[0].prefix)
        assertEquals(2, patterns[0].count)
    }

    @Test
    fun `does not learn a pattern from a carrier-wide prefix`() {
        // Only 7 shared chars ("+628129") — below MIN_PREFIX_LEN, so no pattern.
        val numbers = listOf(
            "+6281299999999",
            "+6281298888888",
        )
        assertTrue(PatternLearner.learn(numbers).isEmpty())
    }

    @Test
    fun `requires at least two numbers`() {
        assertTrue(PatternLearner.learn(listOf("+6285592679948")).isEmpty())
        assertTrue(PatternLearner.learn(emptyList()).isEmpty())
    }

    @Test
    fun `deduplicates patterns discovered from multiple members`() {
        val numbers = listOf(
            "+6285592679948",
            "+6285592679125",
            "+6285592679231",
            "+622135523726",
            "+622135523755",
        )
        val patterns = PatternLearner.learn(numbers)
        assertEquals(2, patterns.size)
        assertEquals(listOf(3, 2), patterns.map { it.count })
    }

    @Test
    fun `matches a number against a learned pattern`() {
        val pattern = BlockPattern("+6285592679", 3, "+6285592679948")
        assertTrue(PatternLearner.matches("+6285592679125", pattern))
        assertTrue(!PatternLearner.matches("+622135523726", pattern))
    }
}
