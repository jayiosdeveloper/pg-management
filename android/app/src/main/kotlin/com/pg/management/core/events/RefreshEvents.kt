package com.pg.management.core.events

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide event bus so a screen that mutates data can ping
 * any list screen that's already on the stack to refresh.
 *
 * Lists collect; mutation sites call `notifyX()` after a successful save.
 */
@Singleton
class RefreshEvents @Inject constructor() {
    private fun makeFlow() = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val _tenants = makeFlow()
    private val _rooms = makeFlow()
    private val _bills = makeFlow()

    val tenantsChanged: SharedFlow<Unit> = _tenants
    val roomsChanged: SharedFlow<Unit> = _rooms
    val billsChanged: SharedFlow<Unit> = _bills

    fun notifyTenantsChanged() { _tenants.tryEmit(Unit) }
    fun notifyRoomsChanged() { _rooms.tryEmit(Unit) }
    fun notifyBillsChanged() { _bills.tryEmit(Unit) }
}
