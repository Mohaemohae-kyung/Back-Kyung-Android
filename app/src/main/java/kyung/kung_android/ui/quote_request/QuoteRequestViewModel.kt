package kyung.kung_android.ui.quote_request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.request.ServiceRequestRepository
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

data class QuoteRequestUiState(
    val expertId: Long = 0L,
    val expertProfileId: Long = 0L,
    val expert: ExpertDetailResponse? = null,
    val categoryId: Long? = null,
    val title: String = "",
    val content: String = "",
    val budgetText: String = "",
    val preferredDate: LocalDate? = null,
    val titleError: String? = null,
    val contentError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
) {
    val canSubmit: Boolean
        get() = title.isNotBlank() &&
            content.isNotBlank() &&
            categoryId != null &&
            !isSubmitting
}

sealed interface QuoteRequestEffect {
    data object NavigateToReceivedQuote : QuoteRequestEffect
}

@HiltViewModel
class QuoteRequestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expertRepository: ExpertRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
) : ViewModel() {

    private val expertId: Long = savedStateHandle.get<Long>("expertId") ?: 0L
    private val expertProfileId: Long = savedStateHandle.get<Long>("expertProfileId") ?: 0L

    private val _state = MutableStateFlow(
        QuoteRequestUiState(expertId = expertId, expertProfileId = expertProfileId)
    )
    val state: StateFlow<QuoteRequestUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<QuoteRequestEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<QuoteRequestEffect> = _effects.asSharedFlow()

    init {
        loadExpert()
    }

    fun onTitleChange(v: String) = _state.update { it.copy(title = v, titleError = null) }
    fun onContentChange(v: String) = _state.update { it.copy(content = v, contentError = null) }
    fun onCategoryChange(id: Long?) = _state.update { it.copy(categoryId = id) }
    fun onBudgetChange(v: String) {
        if (v.isEmpty() || v.all { it.isDigit() }) {
            _state.update { it.copy(budgetText = v) }
        }
    }
    fun onDateChange(date: LocalDate?) = _state.update { it.copy(preferredDate = date) }

    fun onSubmit() {
        val current = _state.value
        if (!current.canSubmit) return
        val categoryId = current.categoryId ?: return

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                serviceRequestRepository.create(
                    expertProfileId = current.expertProfileId,
                    categoryId = categoryId,
                    title = current.title,
                    content = current.content,
                    budget = current.budgetText.takeIf { it.isNotEmpty() }?.let { BigDecimal(it) },
                    preferredDate = current.preferredDate?.atStartOfDay(),
                )
                _effects.emit(QuoteRequestEffect.NavigateToReceivedQuote)
            } catch (e: ApiException) {
                _state.update { it.copy(errorMessage = e.message.ifBlank { "요청 전송에 실패했어요." }) }
            } catch (t: Throwable) {
                val detail = t.message?.takeIf { it.isNotBlank() } ?: t.javaClass.simpleName
                _state.update { it.copy(errorMessage = "요청 전송에 실패했어요. ($detail)") }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun loadExpert() {
        viewModelScope.launch {
            try {
                val expert = expertRepository.getExpertDetail(expertId)
                _state.update { it.copy(expert = expert) }
            } catch (t: Throwable) {
                // 표시만 빈 채로 진행
            }
        }
    }
}
