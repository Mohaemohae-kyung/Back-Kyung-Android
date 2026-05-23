package kyung.kung_android.domain.request

import kyung.kung_android.data.request.api.ServiceRequestApi
import kyung.kung_android.data.request.dto.ServiceRequestCreateRequest
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRequestRepository @Inject constructor(
    private val api: ServiceRequestApi,
) {

    suspend fun create(
        expertServiceId: Long,
        title: String,
        content: String,
        budget: BigDecimal? = null,
        preferredDate: LocalDateTime? = null,
    ): ServiceRequestResponse =
        api.createServiceRequest(
            ServiceRequestCreateRequest(
                expertServiceId = expertServiceId,
                title = title.trim(),
                content = content.trim(),
                budget = budget,
                preferredDate = preferredDate,
            )
        )

    suspend fun getMyRequests(): List<ServiceRequestResponse> = api.getMyServiceRequests()

    suspend fun getRequest(requestId: Long): ServiceRequestResponse =
        api.getServiceRequest(requestId)

    suspend fun cancel(requestId: Long): ServiceRequestResponse =
        api.cancelServiceRequest(requestId)
}
