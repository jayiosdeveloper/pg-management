package com.pg.management.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val FORGOT = "forgot_password"
    const val ADMIN_HOME = "admin_home"
    const val TENANT_HOME = "tenant_home"

    const val TENANT_FORM = "tenant_form?tenantId={tenantId}"
    const val TENANT_DETAIL = "tenant_detail/{tenantId}"
    const val MEMBER_CREDENTIALS = "member_credentials/{tenantId}"

    const val ROOM_FORM = "room_form?roomId={roomId}"

    const val WORKER_FORM = "worker_form?workerId={workerId}"
    const val WORKER_CREDENTIALS = "worker_credentials/{workerId}"

    fun tenantForm(tenantId: String? = null) = "tenant_form?tenantId=${tenantId.orEmpty()}"
    fun tenantDetail(id: String) = "tenant_detail/$id"
    fun memberCredentials(id: String) = "member_credentials/$id"
    fun roomForm(roomId: String? = null) = "room_form?roomId=${roomId.orEmpty()}"
    fun workerForm(workerId: String? = null) = "worker_form?workerId=${workerId.orEmpty()}"
    fun workerCredentials(id: String) = "worker_credentials/$id"
}
