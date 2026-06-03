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
import androidx.navigation.navDeepLink
import kyung.kung_android.ui.account_settings.AccountSettingsScreen
import kyung.kung_android.ui.account_withdraw.AccountWithdrawScreen
import kyung.kung_android.ui.chat_detail.ChatDetailScreen
import kyung.kung_android.ui.chatbot.ChatBotScreen
import kyung.kung_android.ui.checkout.CheckoutScreen
import kyung.kung_android.ui.payment_password.PaymentPasswordSetupScreen
import kyung.kung_android.ui.checkout.TossPaymentScreen
import kyung.kung_android.ui.checkout.PaymentSuccessScreen
import kyung.kung_android.ui.expert_transactions.ExpertTransactionsScreen
import kyung.kung_android.ui.notice_detail.NoticeDetailScreen
import kyung.kung_android.ui.transaction_detail.TransactionDetailScreen
import kyung.kung_android.ui.common.PlaceholderScreen
import kyung.kung_android.ui.expert_detail.ExpertDetailScreen
import kyung.kung_android.ui.expert_register.ExpertRegisterScreen
import kyung.kung_android.ui.favorite_experts.FavoriteExpertsScreen
import kyung.kung_android.ui.main.MainScaffold
import kyung.kung_android.ui.mypage.MyPageScreen
import kyung.kung_android.ui.payment_history.PaymentHistoryScreen
import kyung.kung_android.ui.post_detail.PostDetailScreen
import kyung.kung_android.ui.post_editor.PostEditorScreen
import kyung.kung_android.ui.password_change.PasswordChangeScreen
import kyung.kung_android.ui.profile_info.ProfileInfoScreen
import kyung.kung_android.ui.quote_detail.QuoteDetailScreen
import kyung.kung_android.ui.quote_request.QuoteRequestScreen
import kyung.kung_android.ui.booking.BookingCheckoutScreen
import kyung.kung_android.ui.payment_qr.PaymentQrGenerateScreen
import kyung.kung_android.ui.payment_qr.PaymentQrScanScreen
import kyung.kung_android.ui.store_detail.StoreDetailScreen
import kyung.kung_android.ui.store_editor.StoreEditorScreen

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
                onNavigateStoreDetail = { id -> navController.navigate("${AppRoute.STORE_DETAIL}/$id") },
                onNavigateStoreEditor = { navController.navigate(AppRoute.STORE_EDITOR) },
                onNavigateQuoteDetail = { id -> navController.navigate("${AppRoute.QUOTE_DETAIL}/$id") },
                onNavigatePostDetail = { id -> navController.navigate("${AppRoute.POST_DETAIL}/$id") },
                onNavigateNoticeDetail = { id -> navController.navigate("${AppRoute.NOTICE_DETAIL}/$id") },
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
        ) { backStackEntry ->
            val scannedRequestIdState = backStackEntry.savedStateHandle
                .getStateFlow<Long?>("qrScannedRequestId", null)
                .collectAsStateWithLifecycle()
            androidx.compose.runtime.LaunchedEffect(scannedRequestIdState.value) {
                val rid = scannedRequestIdState.value ?: return@LaunchedEffect
                backStackEntry.savedStateHandle.remove<Long>("qrScannedRequestId")
                backStackEntry.savedStateHandle.remove<String>("qrScannedAmount")
                navController.navigate("${AppRoute.CHECKOUT}/$rid")
            }
            ChatDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateCheckout = { requestId ->
                    navController.navigate("${AppRoute.CHECKOUT}/$requestId")
                },
                onNavigateQrGenerate = { requestId, amount ->
                    navController.navigate(
                        "${AppRoute.PAYMENT_QR_GENERATE}/$requestId/${android.net.Uri.encode(amount)}"
                    )
                },
                onNavigateQrScan = {
                    navController.navigate(AppRoute.PAYMENT_QR_SCAN)
                },
            )
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
                onNavigateQuoteRequest = { expertId, expertProfileId ->
                    navController.navigate("${AppRoute.QUOTE_REQUEST}/$expertId/$expertProfileId")
                },
                onNavigateLogin = { navController.navigate(AppRoute.LOGIN) },
                onNavigateEditProfile = { navController.navigate(AppRoute.EXPERT_REGISTER) },
            )
        }

        composable(
            route = "${AppRoute.QUOTE_REQUEST}/{expertId}/{expertProfileId}",
            arguments = listOf(
                navArgument("expertId") { type = NavType.LongType },
                navArgument("expertProfileId") { type = NavType.LongType },
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
            route = "${AppRoute.QUOTE_DETAIL}/{requestId}?context={context}",
            arguments = listOf(
                navArgument("requestId") { type = NavType.LongType },
                navArgument("context") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val context = entry.arguments?.getString("context")
            val title = if (context == "transaction") "거래 상세" else "견적 상세"
            QuoteDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateExpertDetail = { id -> navController.navigate("${AppRoute.EXPERT_DETAIL}/$id") },
                onNavigateChat = { chatRoomId ->
                    navController.navigate("${AppRoute.CHAT_DETAIL}/$chatRoomId")
                },
                onNavigateCheckout = { requestId ->
                    navController.navigate("${AppRoute.CHECKOUT}/$requestId")
                },
                topBarTitle = title,
            )
        }

        composable(
            route = "${AppRoute.CHECKOUT}/{requestId}",
            arguments = listOf(navArgument("requestId") { type = NavType.LongType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "matchingon://pay?v={v}&rid={requestId}&amt={amt}&exp={exp}" },
            ),
        ) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onNavigateTossPayment = { orderId, amount, method, requestId, orderName ->
                    navController.navigate(
                        "${AppRoute.TOSS_PAYMENT}?orderId=${android.net.Uri.encode(orderId)}&amount=$amount&method=$method&requestId=$requestId&orderName=${android.net.Uri.encode(orderName)}"
                    )
                },
                onNavigatePaymentPasswordSetup = { navController.navigate(AppRoute.PAYMENT_PASSWORD_SETUP) },
            )
        }

        composable(
            route = "${AppRoute.PAYMENT_SUCCESS}/{paymentId}",
            arguments = listOf(navArgument("paymentId") { type = NavType.LongType }),
        ) {
            PaymentSuccessScreen(
                onClose = { navController.popBackStack(AppRoute.MAIN, inclusive = false) },
            )
        }

        composable(
            route = "${AppRoute.PAYMENT_QR_GENERATE}/{requestId}/{amount}",
            arguments = listOf(
                navArgument("requestId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType },
            ),
        ) {
            PaymentQrGenerateScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoute.PAYMENT_QR_SCAN) {
            PaymentQrScanScreen(
                onBack = { navController.popBackStack() },
                onScanned = { requestId, amount ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("qrScannedRequestId", requestId)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("qrScannedAmount", amount)
                    navController.popBackStack()
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
                onNavigatePaymentHistory = { type ->
                    navController.navigate("${AppRoute.PAYMENT_HISTORY}?type=$type")
                },
                onNavigateExpertTransactions = { navController.navigate(AppRoute.EXPERT_TRANSACTIONS) },
                onNavigateExpertSelf = { id ->
                    if (id != null) navController.navigate("${AppRoute.EXPERT_DETAIL}/$id")
                },
            )
        }

        composable(AppRoute.EXPERT_REGISTER) {
            ExpertRegisterScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoute.ACCOUNT_SETTINGS) {
            AccountSettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateProfileInfo = { navController.navigate(AppRoute.PROFILE_INFO) },
                onNavigatePasswordChange = { navController.navigate(AppRoute.PASSWORD_CHANGE) },
                onNavigatePaymentPassword = { navController.navigate(AppRoute.PAYMENT_PASSWORD_SETUP) },
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

        composable(AppRoute.PAYMENT_PASSWORD_SETUP) {
            PaymentPasswordSetupScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
            )
        }

        composable(AppRoute.PASSWORD_CHANGE) {
            PasswordChangeScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
            )
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

        composable(
            route = "${AppRoute.PAYMENT_HISTORY}?type={type}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            PaymentHistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigateTransactionDetail = { paymentId ->
                    navController.navigate("${AppRoute.TRANSACTION_DETAIL}/$paymentId")
                },
            )
        }

        composable(
            route = "${AppRoute.TRANSACTION_DETAIL}/{paymentId}",
            arguments = listOf(navArgument("paymentId") { type = NavType.LongType }),
        ) {
            TransactionDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoute.EXPERT_TRANSACTIONS) {
            ExpertTransactionsScreen(
                onBack = { navController.popBackStack() },
                onNavigateQuoteDetail = { requestId ->
                    navController.navigate("${AppRoute.QUOTE_DETAIL}/$requestId?context=transaction")
                },
            )
        }

        composable(
            route = "${AppRoute.NOTICE_DETAIL}/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.LongType }),
        ) {
            NoticeDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "${AppRoute.STORE_DETAIL}/{storeProductId}",
            arguments = listOf(navArgument("storeProductId") { type = NavType.LongType }),
        ) {
            StoreDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateCheckout = { bookingId ->
                    navController.navigate("${AppRoute.BOOKING_CHECKOUT}/$bookingId")
                },
                onNavigateLogin = { navController.navigate(AppRoute.LOGIN) },
            )
        }

        composable(AppRoute.STORE_EDITOR) {
            StoreEditorScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
            )
        }

        composable(
            route = "${AppRoute.BOOKING_CHECKOUT}/{bookingId}",
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType }),
        ) {
            BookingCheckoutScreen(
                onBack = { navController.popBackStack() },
                onNavigateTossPayment = { orderId, amount, method, orderName ->
                    navController.navigate(
                        "${AppRoute.TOSS_PAYMENT}?orderId=${android.net.Uri.encode(orderId)}&amount=$amount&method=$method&orderName=${android.net.Uri.encode(orderName)}"
                    )
                },
                onNavigatePaymentPasswordSetup = { navController.navigate(AppRoute.PAYMENT_PASSWORD_SETUP) },
            )
        }

        composable(
            route = "${AppRoute.TOSS_PAYMENT}?orderId={orderId}&amount={amount}&method={method}&requestId={requestId}&orderName={orderName}",
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("amount") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("method") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("requestId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("orderName") { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
        ) { backStackEntry ->
            val reqId = backStackEntry.arguments?.getString("requestId")?.toLongOrNull()
            TossPaymentScreen(
                onBack = { navController.popBackStack() },
                onPaymentSuccess = { paymentId ->
                    val popTarget = if (reqId != null) {
                        "${AppRoute.CHECKOUT}/{requestId}"
                    } else {
                        "${AppRoute.BOOKING_CHECKOUT}/{bookingId}"
                    }
                    navController.navigate("${AppRoute.PAYMENT_SUCCESS}/$paymentId") {
                        popUpTo(popTarget) { inclusive = true }
                    }
                },
            )
        }
    }
}
