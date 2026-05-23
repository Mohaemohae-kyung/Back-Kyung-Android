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

    // 5탭 nested NavHost 내부 라우트
    object Tab {
        const val HOME = "home"
        const val EXPERT_SEARCH = "expert_search"
        const val RECEIVED_QUOTE = "received_quote"
        const val CHAT = "chat"
        const val COMMUNITY = "community"
    }
}
