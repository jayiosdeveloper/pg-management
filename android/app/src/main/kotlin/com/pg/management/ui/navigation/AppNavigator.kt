package com.pg.management.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pg.management.ui.screens.admin.AdminHomeScreen
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
                    nav.navigate(Routes.ADMIN_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onLoggedInAsTenant = {
                    nav.navigate(Routes.TENANT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPassword = { nav.navigate(Routes.FORGOT) },
            )
        }

        composable(Routes.FORGOT) {
            ForgotPasswordScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(
                onLogout = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.TENANT_HOME) {
            TenantHomeScreen(
                onLogout = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.TENANT_HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}
