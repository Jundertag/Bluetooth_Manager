package com.jayden.bluetooth.core.exception

import com.jayden.bluetooth.data.adapter.exception.AdapterException

open class PermissionException(
    override val message: String? = null,
    open val permission: String? = null,
    override val cause: Throwable? = null,
) : SecurityException() {
    open fun getMissingPerm(): String? = permission
}