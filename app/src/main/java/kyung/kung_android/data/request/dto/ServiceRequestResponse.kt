package kyung.kung_android.data.request.dto

import kotlinx.serialization.Serializable
import kyung.kung_android.data.serialization.LocalDateTimeSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class ServiceRequestResponse(
    val requestId: Long,
    val userId: Long? = null,
    val expertServiceId: Long? = null,
    val expertProfileId: Long? = null,
    val categoryId: Long? = null,
    val locationId: Long? = null,
    val chatRoomId: Long? = null,
    val title: String,
    val content: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val budget: BigDecimal? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val preferredDate: LocalDateTime? = null,
    val status: String,
    val requesterName: String? = null,
    val unreadCount: Long = 0,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime? = null,
)
