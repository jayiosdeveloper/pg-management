package com.pg.management.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.pg.management.ui.components.GradientBackground
import com.pg.management.ui.screens.admin.billing.AdminBillingScreen
import com.pg.management.ui.screens.admin.dashboard.AdminDashboardScreen
import com.pg.management.ui.screens.admin.more.AdminMoreScreen
import com.pg.management.ui.screens.admin.rooms.RoomsListScreen
import com.pg.management.ui.screens.admin.tenants.TenantsListScreen
import com.pg.management.ui.screens.admin.workers.WorkersListScreen
import com.pg.management.ui.screens.session.SessionViewModel
import com.pg.management.ui.theme.BrandCyan
import com.pg.management.ui.theme.BrandDeepDarker
import com.pg.management.ui.theme.Slate400

@Composable
fun AdminShellScreen(
    onLogout: () -> Unit,
    onOpenTenantDetail: (String) -> Unit,
    onAddTenant: () -> Unit,
    onOpenRoomDetail: (String) -> Unit,
    onAddRoom: () -> Unit,
    onOpenMemberCredentials: (String) -> Unit,
    onOpenWorkerDetail: (String) -> Unit,
    onAddWorker: () -> Unit,
    onOpenWorkerCredentials: (String) -> Unit,
    sessionVm: SessionViewModel = hiltViewModel(),
) {
    var selected by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            AdminTab("Home", Icons.Outlined.Dashboard),
            AdminTab("Members", Icons.Outlined.People),
            AdminTab("Workers", Icons.Outlined.Engineering),
            AdminTab("Rooms", Icons.Outlined.Apartment),
            AdminTab("Bills", Icons.Outlined.Payments),
            AdminTab("More", Icons.Outlined.MoreHoriz),
        )
    }
    val sessionState by sessionVm.state.collectAsState()

    LaunchedEffect(sessionState.loggedOut) {
        if (sessionState.loggedOut) { sessionVm.consumeLogout(); onLogout() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = BrandDeepDarker.copy(alpha = 0.9f), contentColor = Color.White) {
                tabs.forEachIndexed { i, tab ->
                    NavigationBarItem(
                        selected = selected == i,
                        onClick = { selected = i },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandCyan,
                            unselectedIconColor = Slate400,
                            selectedTextColor = BrandCyan,
                            unselectedTextColor = Slate400,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        GradientBackground {
            Box(Modifier.padding(padding)) {
                when (selected) {
                    0 -> AdminDashboardScreen(onSeeAllTenants = { selected = 1 }, onSeeAllRooms = { selected = 3 })
                    1 -> TenantsListScreen(onTenantClick = onOpenTenantDetail, onAddTenant = onAddTenant, onShowInfo = onOpenMemberCredentials)
                    2 -> WorkersListScreen(onWorkerClick = onOpenWorkerDetail, onAddWorker = onAddWorker, onShowInfo = onOpenWorkerCredentials)
                    3 -> RoomsListScreen(onRoomClick = onOpenRoomDetail, onAddRoom = onAddRoom)
                    4 -> AdminBillingScreen()
                    5 -> AdminMoreScreen(onLogout = sessionVm::logout, loggingOut = sessionState.loggingOut, session = sessionState.session)
                }
            }
        }
    }
}

private data class AdminTab(val label: String, val icon: ImageVector)
