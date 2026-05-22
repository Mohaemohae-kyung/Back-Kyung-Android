package kyung.kung_android.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiResponse<T>(
    @SerialName("isSuccess") val isSuccess: Boolean,
    @SerialName("code") val code: String,
    @SerialName("message") val message: String,
    @SerialName("result") val result: T? = null,
)

@Serializable
data class RawApiResponse(
    @SerialName("isSuccess") val isSuccess: Boolean,
    @SerialName("code") val code: String,
    @SerialName("message") val message: String,
    @SerialName("result") val result: JsonElement? = null,
)
