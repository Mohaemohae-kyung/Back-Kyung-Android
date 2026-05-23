package kyung.kung_android.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kyung.kung_android.ui.chat_list.ChatListScreen
import kyung.kung_android.ui.common.LoginGate
import kyung.kung_android.ui.community.CommunityScreen
import kyung.kung_android.ui.expert_search.ExpertSearchScreen
import kyung.kung_android.ui.home.HomeScreen
import kyung.kung_android.ui.navigation.AppRoute
import kyung.kung_android.ui.received_quote.ReceivedQuoteScreen

@Composable
fun MainScaffold(
    onNavigateLogin: () -> Unit,
    onNavigateMyPage: () -> Unit,
    onNavigateExpertDetail: (Long) -> Unit = {},
    onNavigateChatbot: () -> Unit = {},
    onNavigateExpertRegister: () -> Unit = {},
    onNavigateQuoteDetail: (Long) -> Unit = {},
    onNavigatePostDetail: (Long) -> Unit = {},
    onNavigatePostWrite: () -> Unit = {},
    onNavigateChatDetail: (Long) -> Unit = {},
    viewModel: MainScaffoldViewModel = hiltViewModel(),
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentBaseRoute = navBackStackEntry?.destination?.route?.substringBefore("?")
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            MainBottomNavigation(
                currentRoute = currentBaseRoute,
                onTabSelected = { route ->
                    val target = when (route) {
                        AppRoute.Tab.EXPERT_SEARCH -> expertSearchRoute(null, null)
                        else -> route
                    }
                    nestedNavController.navigate(target) {
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
                    onNavigateExpertSearch = { keyword, categoryId ->
                        nestedNavController.navigate(
                            expertSearchRoute(keyword = keyword, categoryId = categoryId)
                        ) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateExpertDetail = onNavigateExpertDetail,
                    onNavigateChatbot = onNavigateChatbot,
                    onNavigateExpertRegister = onNavigateExpertRegister,
                )
            }

            composable(
                route = "${AppRoute.Tab.EXPERT_SEARCH}?keyword={keyword}&categoryId={categoryId}",
                arguments = listOf(
                    navArgument("keyword") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("categoryId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) { backStackEntry ->
                val keyword = backStackEntry.arguments?.getString("keyword")
                val categoryIdRaw = backStackEntry.arguments?.getLong("categoryId") ?: -1L
                val categoryId = categoryIdRaw.takeIf { it != -1L }
                ExpertSearchScreen(
                    initialKeyword = keyword,
                    initialCategoryId = categoryId,
                    onNavigateExpertDetail = onNavigateExpertDetail,
                )
            }

            composable(AppRoute.Tab.RECEIVED_QUOTE) {
                LoginGate(isLoggedIn = isLoggedIn, onNavigateLogin = onNavigateLogin) {
                    ReceivedQuoteScreen(
                        onNavigateExpertSearch = {
                            nestedNavController.navigate(expertSearchRoute(null, null)) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateQuoteDetail = onNavigateQuoteDetail,
                    )
                }
            }

            composable(AppRoute.Tab.CHAT) {
                LoginGate(isLoggedIn = isLoggedIn, onNavigateLogin = onNavigateLogin) {
                    ChatListScreen(onNavigateChat = onNavigateChatDetail)
                }
            }

            composable(AppRoute.Tab.COMMUNITY) {
                CommunityScreen(
                    onNavigatePostDetail = onNavigatePostDetail,
                    onNavigatePostWrite = {
                        if (isLoggedIn) onNavigatePostWrite() else onNavigateLogin()
                    },
                )
            }
        }
    }
}

private fun expertSearchRoute(keyword: String?, categoryId: Long?): String {
    val k = keyword?.takeIf { it.isNotEmpty() }.orEmpty()
    val c = categoryId?.toString() ?: "-1"
    return "${AppRoute.Tab.EXPERT_SEARCH}?keyword=$k&categoryId=$c"
}
