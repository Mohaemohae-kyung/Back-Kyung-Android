package kyung.kung_android.domain.payment

/**
 * 결제 전담 서버가 내려준 오류 응답을 그대로 담는 예외.
 * [code] 는 서버가 보낸 식별자, [message] 는 사용자 노출용 문구.
 */
class PaymentException(
    val httpStatus: Int,
    override val message: String,
    val code: String? = null,
) : RuntimeException(message) {

    /** 결제 비밀번호 불일치. */
    val isWrongPassword: Boolean get() = code == CODE_INVALID_PASSWORD

    /** 5회 실패/관리자 정지 등으로 잠긴 계정. */
    val isLocked: Boolean get() = code == CODE_ACCOUNT_LOCKED

    /** 결제 비밀번호 미설정. */
    val isPasswordNotSet: Boolean get() = code == CODE_PASSWORD_NOT_SET

    companion object {
        const val CODE_INVALID_PASSWORD = "INVALID_PASSWORD"
        const val CODE_ACCOUNT_LOCKED = "ACCOUNT_LOCKED"
        const val CODE_PASSWORD_NOT_SET = "PASSWORD_NOT_SET"
    }
}
