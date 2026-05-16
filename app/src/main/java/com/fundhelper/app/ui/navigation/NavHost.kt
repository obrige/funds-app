package com.fundhelper.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fundhelper.app.ui.home.HomeScreen
import com.fundhelper.app.ui.detail.FundDetailScreen
import com.fundhelper.app.ui.market.MarketCenterScreen
import com.fundhelper.app.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object FundDetail : Screen("fund_detail/{code}") {
        fun createRoute(code: String) = "fund_detail/$code"
    }
    data object MarketCenter : Screen("market_center")
    data object Settings : Screen("settings")
}

@Composable
fun FundNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onFundClick = { code -> navController.navigate(Screen.FundDetail.createRoute(code)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onMarketClick = { navController.navigate(Screen.MarketCenter.route) }
            )
        }

        composable(
            route = Screen.FundDetail.route,
            arguments = listOf(navArgument("code") { type = NavType.StringType })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            FundDetailScreen(
                fundCode = code,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MarketCenter.route) {
            MarketCenterScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
