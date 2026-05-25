package com.pg.management.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pg.management.ui.screens.admin.AdminShellScreen
import com.pg.management.ui.screens.admin.billing.BillDetailScreen
import com.pg.management.ui.screens.admin.credentials.MemberCredentialsScreen
import com.pg.management.ui.screens.admin.rooms.RoomFormScreen
import com.pg.management.ui.screens.admin.tenants.TenantDetailScreen
import com.pg.management.ui.screens.admin.tenants.TenantFormScreen
import com.pg.management.ui.screens.forgot.ForgotPasswordScreen
import com.pg.management.ui.screens.login.LoginScreen
import com.pg.management.ui.screens.tenant.TenantHomeScreen

@Composable
fun AppNavigator(startDestination: String) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { it / 6 }) + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 6 }) + fadeOut() },
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedInAsAdmin = {
                    nav.navigate(Routes.ADMIN_HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onLoggedInAsTenant = {
                    nav.navigate(Routes.TENANT_HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onForgotPassword = { nav.navigate(Routes.FORGOT) },
            )
        }
        composable(Routes.FORGOT) { ForgotPasswordScreen(onBack = { nav.popBackStack() }) }

        composable(Routes.ADMIN_HOME) {
            AdminShellScreen(
                onLogout = {
                    nav.navigate(Routes.LOGIN) { popUpTo(Routes.ADMIN_HOME) { inclusive = true } }
                },
                onOpenTenantDetail = { id -> nav.navigate(Routes.tenantDetail(id)) },
                onAddTenant = { nav.navigate(Routes.tenantForm()) },
                onOpenRoomDetail = { id -> nav.navigate(Routes.roomForm(id)) },
                onAddRoom = { nav.navigate(Routes.roomForm()) },
                onOpenBillDetail = { id -> nav.navigate(Routes.billDetail(id)) },
                onOpenMemberCredentials = { id -> nav.navigate(Routes.memberCredentials(id)) },
            )
        }

        composable(
            route = Routes.TENANT_FORM,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType; defaultValue = "" }),
        ) { backStack ->
            val raw = backStack.arguments?.getString("tenantId").orEmpty()
            TenantFormScreen(
                onBack = { nav.popBackStack() },
                onSaved = { tenantId ->
                    nav.popBackStack()
                    if (raw.isEmpty()) nav.navigate(Routes.tenantDetail(tenantId))
                },
            )
        }

        composable(
            route = Routes.TENANT_DETAIL,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType }),
        ) {
            TenantDetailScreen(
                onBack = { nav.popBackStack() },
                onEdit = { id -> nav.navigate(Routes.tenantForm(id)) },
                onDeleted = { nav.popBackStack() },
            )
        }

        composable(
            route = Routes.MEMBER_CREDENTIALS,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType }),
        ) {
            MemberCredentialsScreen(onBack = { nav.popBackStack() })
        }

        composable(
            route = Routes.ROOM_FORM,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType; defaultValue = "" }),
        ) {
            RoomFormScreen(onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() })
        }

        composable(
            route = Routes.BILL_DETAIL,
            arguments = listOf(navArgument("billId") { type = NavType.StringType }),
        ) {
            BillDetailScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.TENANT_HOME) {
            TenantHomeScreen(
                onLogout = {
                    nav.navigate(Routes.LOGIN) { popUpTo(Routes.TENANT_HOME) { inclusive = true } }
                },
            )
        }
    }
}
