package kyung.kung_android.data.coupon.api

import kyung.kung_android.data.coupon.dto.AvailableCouponDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CouponApi {

    @GET("/api/coupons/usable")
    suspend fun getUsableCoupons(
        @Query("targetType") targetType: String,
        @Query("targetId") targetId: Long,
    ): List<AvailableCouponDto>
}
