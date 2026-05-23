package kyung.kung_android.data.file.api

import kyung.kung_android.data.file.dto.FileUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FileApi {

    @Multipart
    @POST("/api/files")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Query("domain") domain: String,
    ): FileUploadResponse
}
