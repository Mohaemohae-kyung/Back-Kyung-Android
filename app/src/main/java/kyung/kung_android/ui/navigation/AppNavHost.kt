package kyung.kung_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kyung.kung_android.ui.auth.login.LoginScreen
import kyung.kung_android.ui.auth.signup.SignupScreen
import kyung.kung_android.ui.home.HomeScreen

@Composable
fun AppNavHost(
    startDestination: String = AppRoute.LOGIN,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(AppRoute.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.HOME) {
                        popUpTo(AppRoute.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
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

        composable(AppRoute.HOME) {
            HomeScreen(
                onLoggedOut = {
                    navController.navigate(AppRoute.LOGIN) {
                        popUpTo(AppRoute.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
