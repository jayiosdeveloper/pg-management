package com.pg.management.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val FORGOT = "forgot_password"
    const val ADMIN_HOME = "admin_home"
    const val TENANT_HOME = "tenant_home"

    const val TENANT_FORM = "tenant_form?tenantId={tenantId}"
    const val TENANT_DETAIL = "tenant_detail/{tenantId}"

    const val ROOM_FORM = "room_form?roomId={roomId}"

    fun tenantForm(tenantId: String? = null) = "tenant_form?tenantId=${tenantId.orEmpty()}"
    fun tenantDetail(id: String) = "tenant_detail/$id"
    fun roomForm(roomId: String? = null) = "room_form?roomId=${roomId.orEmpty()}"
}
