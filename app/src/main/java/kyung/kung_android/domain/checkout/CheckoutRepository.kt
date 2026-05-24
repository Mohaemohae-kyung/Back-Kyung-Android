package kyung.kung_android.domain.checkout

import kyung.kung_android.data.checkout.api.CheckoutApi
import kyung.kung_android.data.checkout.dto.ServiceRequestCheckoutResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckoutRepository @Inject constructor(
    private val checkoutApi: CheckoutApi,
) {

    suspend fun getServiceRequestCheckout(requestId: Long): ServiceRequestCheckoutResponse =
        checkoutApi.getServiceRequestCheckout(requestId)
}
