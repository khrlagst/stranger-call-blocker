// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

/**
 * Learns spam patterns from blocked numbers by clustering on the longest
 * common prefix: spam campaigns typically use one switch/range and randomize
 * the tail, so several blocked numbers sharing a long prefix reveal the range.
 */
object PatternLearner {

    /** A pattern prefix must be long enough to be specific (e.g. "+6285592679"),
     *  but not so short it matches a whole carrier range ("+62812"). */
    const val MIN_PREFIX_LEN = 8

    /** A pattern needs at least this many supporting numbers. */
    const val MIN_SUPPORT = 2

    /**
     * Finds the longest prefix (>= [MIN_PREFIX_LEN], leaving at least 2 tail
     * digits) shared by >= [MIN_SUPPORT] numbers. Returns patterns ordered by
     * support, deduplicated by prefix.
     */
    fun learn(numbers: List<String>): List<BlockPattern> {
        if (numbers.size < MIN_SUPPORT) return emptyList()
        val found = mutableListOf<BlockPattern>()
        val seen = mutableSetOf<String>()
        for (n in numbers) {
            if (n.length <= MIN_PREFIX_LEN + 1) continue
            var best: BlockPattern? = null
            for (len in (n.length - 2) downTo MIN_PREFIX_LEN) {
                val prefix = n.substring(0, len)
                val matches = numbers.count { it.startsWith(prefix) }
                if (matches >= MIN_SUPPORT) {
                    best = BlockPattern(prefix, matches, n)
                    break
                }
            }
            best?.let { if (seen.add(it.prefix)) found.add(it) }
        }
        return found.sortedByDescending { it.count }
    }

    /** True when [number] starts with the pattern's prefix. */
    fun matches(number: String, pattern: BlockPattern): Boolean =
        number.startsWith(pattern.prefix)
}
