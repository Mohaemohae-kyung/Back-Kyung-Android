package kyung.kung_android.ui.navigation

object AppRoute {

    // 상위 NavController (외부 NavHost)
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MAIN = "main"
    const val MY_PAGE = "my_page"
    const val CHATBOT = "chatbot"
    const val EXPERT_REGISTER = "expert_register"
    const val EXPERT_DETAIL = "expert_detail"
    const val QUOTE_REQUEST = "quote_request"
    const val QUOTE_DETAIL = "quote_detail"
    const val POST_DETAIL = "post_detail"
    const val POST_EDITOR = "post_editor"
    const val CHAT_DETAIL = "chat_detail"
    const val ACCOUNT_SETTINGS = "account_settings"
    const val ACCOUNT_WITHDRAW = "account_withdraw"
    const val PROFILE_INFO = "profile_info"
    const val FAVORITE_EXPERTS = "favorite_experts"
    const val PAYMENT_HISTORY = "payment_history"
    const val CHECKOUT = "checkout"
    const val PAYMENT_SUCCESS = "payment_success"
    const val TRANSACTION_DETAIL = "transaction_detail"
    const val EXPERT_TRANSACTIONS = "expert_transactions"

    const val ARG_CHAT_ROOM_ID = "chatRoomId"
    const val ARG_FORCE_TAB = "forceTab"

    // 5탭 nested NavHost 내부 라우트
    object Tab {
        const val HOME = "home"
        const val EXPERT_SEARCH = "expert_search"
        const val RECEIVED_QUOTE = "received_quote"
        const val CHAT = "chat"
        const val COMMUNITY = "community"
    }
}
