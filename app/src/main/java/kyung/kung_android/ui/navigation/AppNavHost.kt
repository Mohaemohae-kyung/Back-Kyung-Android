package kyung.kung_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.ui.auth.login.LoginScreen
import kyung.kung_android.ui.auth.signup.SignupScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kyung.kung_android.ui.account_settings.AccountSettingsScreen
import kyung.kung_android.ui.account_withdraw.AccountWithdrawScreen
import kyung.kung_android.ui.chat_detail.ChatDetailScreen
import kyung.kung_android.ui.chatbot.ChatBotScreen
import kyung.kung_android.ui.common.PlaceholderScreen
import kyung.kung_android.ui.expert_detail.ExpertDetailScreen
import kyung.kung_android.ui.expert_register.ExpertRegisterScreen
import kyung.kung_android.ui.favorite_experts.FavoriteExpertsScreen
import kyung.kung_android.ui.main.MainScaffold
import kyung.kung_android.ui.mypage.MyPageScreen
import kyung.kung_android.ui.payment_history.PaymentHistoryScreen
import kyung.kung_android.ui.post_detail.PostDetailScreen
import kyung.kung_android.ui.post_editor.PostEditorScreen
import kyung.kung_android.ui.profile_info.ProfileInfoScreen
import kyung.kung_android.ui.quote_detail.QuoteDetailScreen
import kyung.kung_android.ui.quote_request.QuoteRequestScreen

@Composable
fun AppNavHost(
    startDestination: String = AppRoute.MAIN,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(AppRoute.MAIN) { backStackEntry ->
            val forceTab by backStackEntry.savedStateHandle
                .getStateFlow<String?>(AppRoute.ARG_FORCE_TAB, null)
                .collectAsStateWithLifecycle()
            MainScaffold(
                onNavigateLogin = { navController.navigate(AppRoute.LOGIN) },
                onNavigateMyPage = { navController.navigate(AppRoute.MY_PAGE) },
                onNavigateChatbot = { navController.navigate(AppRoute.CHATBOT) },
                onNavigateExpertRegister = { navController.navigate(AppRoute.EXPERT_REGISTER) },
                onNavigateSignup = { navController.navigate(AppRoute.SIGNUP) },
                onNavigateExpertDetail = { id -> navController.navigate("${AppRoute.EXPERT_DETAIL}/$id") },
                onNavigateQuoteDetail = { id -> navController.navigate("${AppRoute.QUOTE_DETAIL}/$id") },
                onNavigatePostDetail = { id -> navController.navigate("${AppRoute.POST_DETAIL}/$id") },
                onNavigatePostWrite = { navController.navigate(AppRoute.POST_EDITOR) },
                onNavigateChatDetail = { chatRoomId ->
                    navController.navigate("${AppRoute.CHAT_DETAIL}/$chatRoomId")
                },
                forceTab = forceTab,
                onForceTabConsumed = {
                    backStackEntry.savedStateHandle[AppRoute.ARG_FORCE_TAB] = null
                },
            )
        }

        composable(
            route = "${AppRoute.CHAT_DETAIL}/{${AppRoute.ARG_CHAT_ROOM_ID}}",
            arguments = listOf(navArgument( AppRoute.ARG_CHAT_ROOM_ID) { type = NavType.LongType }),
        ) {
            ChatDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = "${AppRoute.POST_DETAIL}/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.LongType }),
        ) {
            PostDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateLogin = { navController.navigate(AppRoute.LOGIN) },
            )
        }

        composable(AppRoute.POST_EDITOR) {
            PostEditorScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "${AppRoute.EXPERT_DETAIL}/{expertId}",
            arguments = listOf(navArgument("expertId") { type = NavType.LongType }),
        ) {
            ExpertDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateQuoteRequest = { expertId, expertServiceId ->
                    navController.navigate("${AppRoute.QUOTE_REQUEST}/$expertId/$expertServiceId")
                },
                onNavigateLogin = { navController.navigate(AppRoute.LOGIN) },
            )
        }

        composable(
            route = "${AppRoute.QUOTE_REQUEST}/{expertId}/{expertServiceId}",
            arguments = listOf(
                navArgument("expertId") { type = NavType.LongType },
                navArgument("expertServiceId") { type = NavType.LongType },
            ),
        ) {
            QuoteRequestScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.getBackStackEntry(AppRoute.MAIN)
                        .savedStateHandle[AppRoute.ARG_FORCE_TAB] = AppRoute.Tab.RECEIVED_QUOTE
                    navController.popBackStack(AppRoute.MAIN, inclusive = false)
                },
            )
        }

        composable(
            route = "${AppRoute.QUOTE_DETAIL}/{requestId}",
            arguments = listOf(navArgument("requestId") { type = NavType.LongType }),
        ) {
            QuoteDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateExpertDetail = { id -> navController.navigate("${AppRoute.EXPERT_DETAIL}/$id") },
                onNavigateChat = { chatRoomId ->
                    navController.navigate("${AppRoute.CHAT_DETAIL}/$chatRoomId")
                },
            )
        }

        composable(AppRoute.CHATBOT) {
            ChatBotScreen(onClose = { navController.popBackStack() })
        }

        composable(AppRoute.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.popBackStack(AppRoute.MAIN, inclusive = false)
                },
                onNavigateSignup = {
                    navController.navigate(AppRoute.SIGNUP)
                },
            )
        }

        composable(AppRoute.SIGNUP) {
            SignupScreen(
                onSignupSuccess = {
                    navController.popBackStack(AppRoute.LOGIN, inclusive = false)
                },
            )
        }

        composable(AppRoute.MY_PAGE) {
            MyPageScreen(
                onBack = { navController.popBackStack() },
                onNavigateLogin = { navController.navigate(AppRoute.LOGIN) },
                onNavigateExpertRegister = { navController.navigate(AppRoute.EXPERT_REGISTER) },
                onNavigateAccountSettings = { navController.navigate(AppRoute.ACCOUNT_SETTINGS) },
                onNavigateFavorites = { navController.navigate(AppRoute.FAVORITE_EXPERTS) },
                onNavigatePaymentHistory = { navController.navigate(AppRoute.PAYMENT_HISTORY) },
            )
        }

        composable(AppRoute.EXPERT_REGISTER) {
            ExpertRegisterScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoute.ACCOUNT_SETTINGS) {
            AccountSettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateProfileInfo = { navController.navigate(AppRoute.PROFILE_INFO) },
                onNavigateWithdraw = { navController.navigate(AppRoute.ACCOUNT_WITHDRAW) },
                onLoggedOut = {
                    navController.popBackStack(AppRoute.MAIN, inclusive = false)
                },
            )
        }

        composable(AppRoute.ACCOUNT_WITHDRAW) {
            AccountWithdrawScreen(
                onBack = { navController.popBackStack() },
                onWithdrawSuccess = {
                    navController.popBackStack(AppRoute.MAIN, inclusive = false)
                    navController.navigate(AppRoute.LOGIN)
                },
            )
        }

        composable(AppRoute.PROFILE_INFO) {
            ProfileInfoScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoute.FAVORITE_EXPERTS) {
            FavoriteExpertsScreen(
                onBack = { navController.popBackStack() },
                onNavigateExpertDetail = { id ->
                    navController.navigate("${AppRoute.EXPERT_DETAIL}/$id")
                },
                onNavigateExpertSearch = {
                    navController.getBackStackEntry(AppRoute.MAIN)
                        .savedStateHandle[AppRoute.ARG_FORCE_TAB] = AppRoute.Tab.EXPERT_SEARCH
                    navController.popBackStack(AppRoute.MAIN, inclusive = false)
                },
            )
        }

        composable(AppRoute.PAYMENT_HISTORY) {
            PaymentHistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
