package com.jayden.bluetooth.adapter.exception

/**
 * Generic Local Adapter Errors
 */
open class AdapterException(
    override val message: String? = null,
    open val state: Int? = null
) : Exception() {
    open fun state(): Int? = state
}