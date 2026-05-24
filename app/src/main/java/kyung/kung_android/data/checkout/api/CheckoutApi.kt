package kyung.kung_android.data.checkout.api

import kyung.kung_android.data.checkout.dto.ServiceRequestCheckoutResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CheckoutApi {

    @GET("/api/checkout/service-requests/{requestId}")
    suspend fun getServiceRequestCheckout(
        @Path("requestId") requestId: Long,
    ): ServiceRequestCheckoutResponse
}
