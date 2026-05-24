package kyung.kung_android.ui.main

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kyung.kung_android.ui.common.InitialAvatar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onNavigateLogin: () -> Unit,
    onNavigateMyPage: () -> Unit,
    onNavigateExpertDetail: (Long) -> Unit = {},
    onNavigateChatbot: () -> Unit = {},
    onNavigateExpertRegister: () -> Unit = {},
    onNavigateSignup: () -> Unit = {},
    onNavigateQuoteDetail: (Long) -> Unit = {},
    onNavigatePostDetail: (Long) -> Unit = {},
    onNavigatePostWrite: () -> Unit = {},
    onNavigateChatDetail: (Long) -> Unit = {},
    forceTab: String? = null,
    onForceTabConsumed: () -> Unit = {},
    viewModel: MainScaffoldViewModel = hiltViewModel(),
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentBaseRoute = navBackStackEntry?.destination?.route?.substringBefore("?")
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isExpert by viewModel.isExpert.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(forceTab) {
        val tab = forceTab ?: return@LaunchedEffect
        val target = when (tab) {
            AppRoute.Tab.EXPERT_SEARCH -> expertSearchRoute(null, null, null)
            else -> tab
        }
        nestedNavController.navigate(target) {
            popUpTo(nestedNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        onForceTabConsumed()
    }

    val tabTitle = when (currentBaseRoute) {
        AppRoute.Tab.EXPERT_SEARCH -> "고수찾기"
        AppRoute.Tab.RECEIVED_QUOTE -> "받은견적"
        AppRoute.Tab.CHAT -> "채팅"
        AppRoute.Tab.COMMUNITY -> "커뮤니티"
        else -> null
    }

    Scaffold(
        topBar = {
            if (tabTitle != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.4).sp,
                            ),
                        )
                    },
                    actions = {
                        val avatarUrl = currentUser?.profileImageUrl
                        val displayName = currentUser?.nickname?.takeIf { it.isNotBlank() }
                            ?: currentUser?.name
                            ?: "?"
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "마이페이지",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onNavigateMyPage),
                            )
                        } else if (currentUser != null) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onNavigateMyPage),
                            ) {
                                InitialAvatar(name = displayName, size = 32.dp)
                            }
                        } else {
                            IconButton(onClick = onNavigateMyPage) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "마이페이지",
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        },
        bottomBar = {
            MainBottomNavigation(
                currentRoute = currentBaseRoute,
                onTabSelected = { route ->
                    val target = when (route) {
                        AppRoute.Tab.EXPERT_SEARCH -> expertSearchRoute(null, null, null)
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
                    isExpert = isExpert,
                    onNavigateLogin = onNavigateLogin,
                    onNavigateMyPage = onNavigateMyPage,
                    onNavigateExpertSearch = { keyword, categoryId, locationId ->
                        nestedNavController.navigate(
                            expertSearchRoute(keyword = keyword, categoryId = categoryId, locationId = locationId)
                        ) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            // restoreState 미사용: 홈에서 시작한 검색은 새 args가 보장돼야 함.
                            // restoreState=true 시 이전에 저장된 back stack entry의 옛 args가 복원되어 새 keyword/category가 무시됨.
                        }
                    },
                    onNavigateExpertDetail = onNavigateExpertDetail,
                    onNavigateChatbot = onNavigateChatbot,
                    onNavigateExpertRegister = onNavigateExpertRegister,
                )
            }

            composable(
                route = "${AppRoute.Tab.EXPERT_SEARCH}?keyword={keyword}&categoryId={categoryId}&locationId={locationId}",
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
                    navArgument("locationId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) { backStackEntry ->
                val keyword = backStackEntry.arguments?.getString("keyword")
                val categoryIdRaw = backStackEntry.arguments?.getLong("categoryId") ?: -1L
                val categoryId = categoryIdRaw.takeIf { it != -1L }
                val locationIdRaw = backStackEntry.arguments?.getLong("locationId") ?: -1L
                val locationId = locationIdRaw.takeIf { it != -1L }
                ExpertSearchScreen(
                    initialKeyword = keyword,
                    initialCategoryId = categoryId,
                    initialLocationId = locationId,
                    onNavigateExpertDetail = onNavigateExpertDetail,
                )
            }

            composable(AppRoute.Tab.RECEIVED_QUOTE) {
                LoginGate(isLoggedIn = isLoggedIn, onNavigateLogin = onNavigateLogin) {
                    ReceivedQuoteScreen(
                        onNavigateExpertSearch = {
                            nestedNavController.navigate(expertSearchRoute(null, null, null)) {
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
                    onNavigateSignup = onNavigateSignup,
                    onNavigateExpertRegister = onNavigateExpertRegister,
                )
            }
        }
    }
}

private fun expertSearchRoute(keyword: String?, categoryId: Long?, locationId: Long?): String {
    val k = keyword?.takeIf { it.isNotEmpty() }?.let(Uri::encode).orEmpty()
    val c = categoryId?.toString() ?: "-1"
    val l = locationId?.toString() ?: "-1"
    return "${AppRoute.Tab.EXPERT_SEARCH}?keyword=$k&categoryId=$c&locationId=$l"
}
