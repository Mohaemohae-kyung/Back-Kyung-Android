package kyung.kung_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kyung.kung_android.ui.auth.login.LoginScreen
import kyung.kung_android.ui.auth.signup.SignupScreen
import kyung.kung_android.ui.chatbot.ChatBotScreen
import kyung.kung_android.ui.common.PlaceholderScreen
import kyung.kung_android.ui.expert_register.ExpertRegisterScreen
import kyung.kung_android.ui.main.MainScaffold
import kyung.kung_android.ui.mypage.MyPageScreen

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
