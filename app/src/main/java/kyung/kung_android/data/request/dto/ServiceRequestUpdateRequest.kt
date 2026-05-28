package kyung.kung_android.data.request.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class ServiceRequestUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val budget: BigDecimal? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val preferredDate: LocalDateTime? = null,
)
