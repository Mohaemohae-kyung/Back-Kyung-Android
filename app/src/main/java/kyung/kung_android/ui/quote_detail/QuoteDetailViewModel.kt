package kyung.kung_android.ui.quote_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.request.dto.ServiceRequestResponse
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.request.ServiceRequestRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class QuoteDetailUiState(
    val requestId: Long = 0L,
    val quote: ServiceRequestResponse? = null,
    val expert: ExpertDetailResponse? = null,
    val myUserId: Long? = null,
    val myRole: String? = null,
    val isLoading: Boolean = true,
    val isActing: Boolean = false,
    val error: String? = null,
) {
    /** 본인이 이 견적의 요청자(USER) 입장 */
    val isRequester: Boolean
        get() = quote?.userId != null && myUserId != null && quote.userId == myUserId

    /** 본인이 이 견적을 받은 EXPERT 입장 (요청자가 아니고 EXPERT/ADMIN 역할) */
    val isReceivingExpert: Boolean
        get() = !isRequester && (myRole == "EXPERT" || myRole == "ADMIN")
}

@HiltViewModel
class QuoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val expertRepository: ExpertRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val requestId: Long = savedStateHandle.get<Long>("requestId") ?: 0L

    private val _state = MutableStateFlow(QuoteDetailUiState(requestId = requestId))
    val state: StateFlow<QuoteDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.currentUser.collect { me ->
                _state.update { it.copy(myUserId = me?.userId, myRole = me?.role) }
            }
        }
    }

    fun onCancel() = act { serviceRequestRepository.cancel(requestId) }
    fun onApprove() = act { serviceRequestRepository.approve(requestId) }
    fun onReject() = act { serviceRequestRepository.reject(requestId) }

    private fun act(call: suspend () -> ServiceRequestResponse) {
        if (_state.value.isActing) return
        _state.update { it.copy(isActing = true, error = null) }
        viewModelScope.launch {
            try {
                val updated = call()
                _state.update { it.copy(quote = updated, isActing = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(isActing = false, error = "처리에 실패했어요.") }
            }
        }
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val quote = serviceRequestRepository.getRequest(requestId)
                _state.update { it.copy(quote = quote, isLoading = false) }
                quote.expertProfileId?.let { id ->
                    runCatching { expertRepository.getExpertDetail(id) }
                        .onSuccess { expert -> _state.update { it.copy(expert = expert) } }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "정보를 불러오지 못했어요.") }
            }
        }
    }
}
