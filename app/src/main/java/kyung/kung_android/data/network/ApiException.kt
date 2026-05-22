package kyung.kung_android.data.network

class ApiException(
    val errorCode: String,
    override val message: String,
    val fieldErrors: Map<String, String>? = null,
    val httpStatus: Int? = null,
) : RuntimeException(message) {

    val isAuthError: Boolean
        get() = errorCode.startsWith("AUTH_401") || errorCode == "COMMON_401"

    val isValidationError: Boolean
        get() = errorCode == "COMMON_400" && fieldErrors != null

    companion object {
        const val CODE_NETWORK = "NETWORK_ERROR"
        const val CODE_UNKNOWN = "UNKNOWN_ERROR"
        const val CODE_EMPTY_BODY = "EMPTY_BODY"
    }
}
