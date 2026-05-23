package kyung.kung_android.data.request.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class ServiceRequestCreateRequest(
    val expertServiceId: Long,
    val title: String,
    val content: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val budget: BigDecimal? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val preferredDate: LocalDateTime? = null,
)
