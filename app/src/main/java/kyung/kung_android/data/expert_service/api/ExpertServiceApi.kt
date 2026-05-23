package kyung.kung_android.data.expert_service.api

import kyung.kung_android.data.expert_service.dto.ExpertServiceCreateRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ExpertServiceApi {

    @POST("/api/expert-services")
    suspend fun createService(
        @Body request: ExpertServiceCreateRequest,
    ): Unit?
}
