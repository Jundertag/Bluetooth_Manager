package com.jayden.bluetooth.data.adapter.exception

open class PermissionException(
    override val message: String? = null,
    open val permission: String? = null
) : AdapterException() {
    open fun getMissingPerm(): String? = permission
}