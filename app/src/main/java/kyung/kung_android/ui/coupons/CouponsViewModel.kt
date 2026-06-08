package kyung.kung_android.ui.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.coupon.dto.AvailableCouponDto
import kyung.kung_android.domain.coupon.CouponRepository
import javax.inject.Inject

data class CouponsUiState(
    val isLoading: Boolean = true,
    val coupons: List<AvailableCouponDto> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class CouponsViewModel @Inject constructor(
    private val couponRepository: CouponRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CouponsUiState())
    val state: StateFlow<CouponsUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val coupons = couponRepository.getMyCoupons()
                _state.update { it.copy(isLoading = false, coupons = coupons) }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "쿠폰을 불러오지 못했어요.") }
            }
        }
    }
}
