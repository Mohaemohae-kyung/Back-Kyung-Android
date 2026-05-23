package kyung.kung_android.domain.expert_service

import kyung.kung_android.data.expert_service.api.ExpertServiceApi
import kyung.kung_android.data.expert_service.dto.ExpertServiceCreateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpertServiceRepository @Inject constructor(
    private val expertServiceApi: ExpertServiceApi,
) {

    suspend fun createService(
        categoryId: Long,
        locationId: Long,
        serviceTitle: String,
        serviceDescription: String,
        price: Int = 0,
    ) {
        expertServiceApi.createService(
            ExpertServiceCreateRequest(
                categoryId = categoryId,
                locationId = locationId,
                serviceTitle = serviceTitle.trim(),
                serviceDescription = serviceDescription.trim(),
                price = price,
            )
        )
    }
}
