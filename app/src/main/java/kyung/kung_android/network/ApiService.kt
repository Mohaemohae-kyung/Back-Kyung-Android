package kyung.kung_android.network

import kyung.kung_android.dto.AppIntegrityReportRequest
import kyung.kung_android.dto.AppIntegrityReportResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/api/app-integrity/report")
    suspend fun reportAppIntegrity(
        @Body request: AppIntegrityReportRequest
    ): AppIntegrityReportResponse
}