package kyung.kung_android.data.payment.api

import kyung.kung_android.data.payment.dto.MockPgApproveRequest
import kyung.kung_android.data.payment.dto.MockPgApproveResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MockPgApi {

    @POST("/api/mock-pg/approve")
    suspend fun approve(@Body request: MockPgApproveRequest): MockPgApproveResponse
}
