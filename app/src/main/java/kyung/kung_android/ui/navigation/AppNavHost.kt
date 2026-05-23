package kyung.kung_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kyung.kung_android.ui.auth.login.LoginScreen
import kyung.kung_android.ui.auth.signup.SignupScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kyung.kung_android.ui.chat_detail.ChatDetailScreen
import kyung.kung_android.ui.chatbot.ChatBotScreen
import kyung.kung_android.ui.common.PlaceholderScreen
import kyung.kung_android.ui.expert_detail.ExpertDetailScreen
import kyung.kung_android.ui.expert_register.ExpertRegisterScreen
import kyung.kung_android.ui.main.MainScaffold
import kyung.kung_android.ui.mypage.MyPageScreen
import kyung.kung_android.ui.post_detail.PostDetailScreen
import kyung.kung_android.ui.post_editor.PostEditorScreen
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
        composable(AppRoute.MAIN) {
            MainScaffold(
                onNavigateLogin = { navController.navigate(AppRoute.LOGIN) },
                onNavigateMyPage = { navController.navigate(AppRoute.MY_PAGE) },
                onNavigateChatbot = { navController.navigate(AppRoute.CHATBOT) },
                onNavigateExpertRegister = { navController.navigate(AppRoute.EXPERT_REGISTER) },
                onNavigateExpertDetail = { id -> navController.navigate("${AppRoute.EXPERT_DETAIL}/$id") },
                onNavigateQuoteDetail = { id -> navController.navigate("${AppRoute.QUOTE_DETAIL}/$id") },
                onNavigatePostDetail = { id -> navController.navigate("${AppRoute.POST_DETAIL}/$id") },
                onNavigatePostWrite = { navController.navigate(AppRoute.POST_EDITOR) },
                onNavigateChatDetail = { chatRoomId ->
                    navController.navigate("${AppRoute.CHAT_DETAIL}/$chatRoomId")
                },
            )
        }

        composable(
            route = "${AppRoute.CHAT_DETAIL}/{${AppRoute.ARG_CHAT_ROOM_ID}}",
            arguments = listOf(navArgument(AppRoute.ARG_CHAT_ROOM_ID) { type = NavType.LongType }),
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
            )
        }

        composable(AppRoute.EXPERT_REGISTER) {
            ExpertRegisterScreen(onBack = { navController.popBackStack() })
        }
    }
}
