package kyung.kung_android.domain.coupon

import kyung.kung_android.data.coupon.api.CouponApi
import kyung.kung_android.data.coupon.dto.AvailableCouponDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(
    private val couponApi: CouponApi,
) {
    suspend fun getUsableCoupons(targetType: String, targetId: Long): List<AvailableCouponDto> =
        couponApi.getUsableCoupons(targetType = targetType, targetId = targetId)

    /**
     * 보유(사용 가능) 쿠폰 전체 조회. 서버의 usable 엔드포인트는 대상 파라미터를 무시하고
     * 로그인 사용자의 사용 가능 쿠폰 전체를 반환하므로 그대로 활용한다.
     */
    suspend fun getMyCoupons(): List<AvailableCouponDto> =
        couponApi.getUsableCoupons(targetType = "SERVICE_REQUEST", targetId = 0L)
}
