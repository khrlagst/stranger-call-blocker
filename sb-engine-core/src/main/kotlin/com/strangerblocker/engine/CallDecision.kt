// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

/**
 * Framework-level decision for an incoming call. The host turns BLOCK* into a
 * rejected call response; ALLOW lets the call ring through.
 */
enum class CallDecision {
    ALLOW,
    BLOCK,
    BLOCK_PRIVATE,
    BLOCK_VOIP,
}
