// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

/** A learned spam pattern — a long prefix shared by several blocked numbers. */
data class BlockPattern(val prefix: String, val count: Int, val example: String)
