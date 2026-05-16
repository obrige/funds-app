package com.fundhelper.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fundhelper.app.data.model.IndexDisplayItem
import com.fundhelper.app.data.model.IndexEntity
import com.fundhelper.app.data.model.IndexQuoteItem
import com.fundhelper.app.ui.home.HomeScreen
import com.fundhelper.app.ui.detail.FundDetailScreen
import com.fundhelper.app.ui.detail.IndexDetailScreen
import com.fundhelper.app.ui.market.MarketCenterScreen
import com.fundhelper.app.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object FundDetail : Screen("fund_detail/{code}") {
        fun createRoute(code: String) = "fund_detail/$code"
    }
    data object IndexDetail : Screen("index_detail/{secId}/{name}/{code}/{market}/{price}/{changeRate}/{change}") {
        fun createRoute(item: IndexDisplayItem): String {
            val q = item.quote
            return "index_detail/${item.entity.secId}/${item.entity.name}/${item.entity.code}" +
                    "/${item.entity.market}/${q?.price ?: 0}/${q?.changeRate ?: 0}/${q?.change ?: 0}"
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
                navArgument("code") { type = NavType.StringType },
                navArgument("market") { type = NavType.IntType },
                navArgument("price") { type = NavType.FloatType },
                navArgument("changeRate") { type = NavType.FloatType },
                navArgument("change") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val item = IndexDisplayItem(
                entity = IndexEntity(
                    secId = args.getString("secId")!!,
                    name = args.getString("name")!!,
                    code = args.getString("code")!!,
                    market = args.getInt("market")
                ),
                quote = IndexQuoteItem(
                    price = args.getDouble("price"),
                    changeRate = args.getDouble("changeRate"),
                    change = args.getDouble("change"),
                    code = args.getString("code")!!,
                    market = args.getInt("market"),
                    name = args.getString("name")!!,
                    amount = null
                )
            )
            IndexDetailScreen(
                item = item,
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
