// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

/** Pure number-shape and range helpers shared by all screening paths. */
object NumberRules {

    /** Sentinel phone number stored for blocked private/unknown calls (no number on the handle). */
    const val PRIVATE_NUMBER_LABEL = "(private)"

    /** True when [number] carries an actual phone number (7–15 digits). */
    fun isPhoneNumberShape(number: String): Boolean {
        val digits = number.count { it.isDigit() }
        return digits in 7..15
    }

    /** Normalizes a sender for comparison: keeps letters and digits only. */
    fun normalizeSender(sender: String): String = sender.filter { it.isLetterOrDigit() }

    /** True when [millis] falls within [from]..[to] (null bounds are open). */
    fun inRange(millis: Long, from: Long?, to: Long?): Boolean =
        (from == null || millis >= from) && (to == null || millis <= to)
}
