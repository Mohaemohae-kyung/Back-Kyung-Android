package kyung.kung_android.data.request.api

import kyung.kung_android.data.request.dto.ServiceRequestCreateRequest
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.data.request.dto.ServiceRequestUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ServiceRequestApi {

    @POST("/api/service-requests")
    suspend fun createServiceRequest(
        @Body request: ServiceRequestCreateRequest,
    ): ServiceRequestResponse

    @PATCH("/api/service-requests/{requestId}")
    suspend fun updateServiceRequest(
        @Path("requestId") requestId: Long,
        @Body request: ServiceRequestUpdateRequest,
    ): ServiceRequestResponse

    @GET("/api/service-requests/me")
    suspend fun getMyServiceRequests(): List<ServiceRequestResponse>

    @GET("/api/service-requests/received")
    suspend fun getReceivedServiceRequests(): List<ServiceRequestResponse>

    @GET("/api/service-requests/{requestId}")
    suspend fun getServiceRequest(
        @Path("requestId") requestId: Long,
    ): ServiceRequestResponse

    @PATCH("/api/service-requests/{requestId}/cancel")
    suspend fun cancelServiceRequest(
        @Path("requestId") requestId: Long,
    ): ServiceRequestResponse

    @PATCH("/api/service-requests/{requestId}/approve")
    suspend fun approveServiceRequest(
        @Path("requestId") requestId: Long,
    ): ServiceRequestResponse

    @PATCH("/api/service-requests/{requestId}/reject")
    suspend fun rejectServiceRequest(
        @Path("requestId") requestId: Long,
    ): ServiceRequestResponse
}
