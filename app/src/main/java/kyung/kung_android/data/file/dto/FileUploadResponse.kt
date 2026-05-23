package kyung.kung_android.data.file.dto

import kotlinx.serialization.Serializable

@Serializable
data class FileUploadResponse(
    val fileId: Long,
    val storedName: String? = null,
    val fileUrl: String? = null,
)
