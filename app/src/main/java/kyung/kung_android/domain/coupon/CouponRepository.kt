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
}
