package kyung.kung_android.ui.store

import kyung.kung_android.data.store.dto.StoreProductResponse
import java.text.NumberFormat
import java.util.Locale

private val PRICE_FMT = NumberFormat.getNumberInstance(Locale.KOREA)

internal fun formatPrice(product: StoreProductResponse): String =
    product.price?.let { "${PRICE_FMT.format(it)}원" } ?: "가격 협의"
