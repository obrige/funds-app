package com.fundhelper.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fundhelper.app.data.model.IndexDisplayItem
import com.fundhelper.app.ui.home.HomeScreen
import com.fundhelper.app.ui.detail.FundDetailScreen
import com.fundhelper.app.ui.detail.IndexDetailScreen
import com.fundhelper.app.ui.market.MarketCenterScreen
import com.fundhelper.app.ui.settings.SettingsScreen
import java.net.URLEncoder
import java.net.URLDecoder

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object FundDetail : Screen("fund_detail/{code}") {
        fun createRoute(code: String) = "fund_detail/$code"
    }
    data object IndexDetail : Screen("index_detail/{secId}/{name}/{code}") {
        fun createRoute(item: IndexDisplayItem): String {
            val sid = URLEncoder.encode(item.entity.secId, "UTF-8")
            val nm = URLEncoder.encode(item.entity.name, "UTF-8")
            val cd = URLEncoder.encode(item.entity.code, "UTF-8")
            return "index_detail/$sid/$nm/$cd"
        }
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
                onMarketClick = { navController.navigate(Screen.MarketCenter.route) },
                onIndexClick = { item -> navController.navigate(Screen.IndexDetail.createRoute(item)) }
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

        composable(
            route = Screen.IndexDetail.route,
            arguments = listOf(
                navArgument("secId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("code") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val secId = URLDecoder.decode(args.getString("secId") ?: "", "UTF-8")
            val name = URLDecoder.decode(args.getString("name") ?: "", "UTF-8")
            val code = URLDecoder.decode(args.getString("code") ?: "", "UTF-8")
            IndexDetailScreen(
                secId = secId,
                name = name,
                code = code,
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
