// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

/** User-facing spam categories for locally-labeled numbers. */
enum class SpamLabel(val display: String) {
    SPAM("Spam"),
    SCAM("Scam"),
    TELEMARKETER("Telemarketer"),
    PROMO("Promo"),
}
