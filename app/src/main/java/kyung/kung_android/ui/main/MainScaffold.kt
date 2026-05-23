package kyung.kung_android.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kyung.kung_android.ui.common.LoginGate
import kyung.kung_android.ui.common.PlaceholderScreen
import kyung.kung_android.ui.home.HomeScreen
import kyung.kung_android.ui.navigation.AppRoute

@Composable
fun MainScaffold(
    onNavigateLogin: () -> Unit,
    onNavigateMyPage: () -> Unit,
    viewModel: MainScaffoldViewModel = hiltViewModel(),
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            MainBottomNavigation(
                currentRoute = currentRoute,
                onTabSelected = { route ->
                    nestedNavController.navigate(route) {
                        popUpTo(nestedNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = AppRoute.Tab.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoute.Tab.HOME) {
                HomeScreen(
                    isLoggedIn = isLoggedIn,
                    onNavigateLogin = onNavigateLogin,
                    onNavigateMyPage = onNavigateMyPage,
                )
            }

            composable(AppRoute.Tab.EXPERT_SEARCH) {
                PlaceholderScreen(title = "고수찾기")
            }

            composable(AppRoute.Tab.RECEIVED_QUOTE) {
                LoginGate(isLoggedIn = isLoggedIn, onNavigateLogin = onNavigateLogin) {
                    PlaceholderScreen(title = "받은견적")
                }
            }

            composable(AppRoute.Tab.CHAT) {
                LoginGate(isLoggedIn = isLoggedIn, onNavigateLogin = onNavigateLogin) {
                    PlaceholderScreen(title = "채팅")
                }
            }

            composable(AppRoute.Tab.COMMUNITY) {
                PlaceholderScreen(title = "커뮤니티")
            }
        }
    }
}
