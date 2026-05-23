package kyung.kung_android.ui.expert_register

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
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.domain.expert.ExpertRepository
import javax.inject.Inject

data class ExpertRegisterUiState(
    val displayName: String = "",
    val introduction: String = "",
    val careerYears: String = "",
    val mainCategoryId: Long? = null,
    val mainLocationId: Long? = null,
    val displayNameError: String? = null,
    val introductionError: String? = null,
    val careerYearsError: String? = null,
    val categoryError: String? = null,
    val locationError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
) {
    val canSubmit: Boolean
        get() = displayName.isNotBlank() &&
                introduction.isNotBlank() &&
                careerYears.toLongOrNull()?.let { it >= 0 } == true &&
                mainCategoryId != null &&
                mainLocationId != null &&
                !isSubmitting
}

sealed interface ExpertRegisterEffect {
    data object NavigateBack : ExpertRegisterEffect
}

@HiltViewModel
class ExpertRegisterViewModel @Inject constructor(
    private val expertRepository: ExpertRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExpertRegisterUiState())
    val state: StateFlow<ExpertRegisterUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ExpertRegisterEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<ExpertRegisterEffect> = _effects.asSharedFlow()

    fun onDisplayNameChange(v: String) = _state.update { it.copy(displayName = v, displayNameError = null) }
    fun onIntroductionChange(v: String) = _state.update { it.copy(introduction = v, introductionError = null) }
    fun onCareerYearsChange(v: String) {
        if (v.isEmpty() || v.all { it.isDigit() }) {
            _state.update { it.copy(careerYears = v, careerYearsError = null) }
        }
    }
    fun onCategorySelected(id: Long) = _state.update { it.copy(mainCategoryId = id, categoryError = null) }
    fun onLocationSelected(id: Long) = _state.update { it.copy(mainLocationId = id, locationError = null) }

    fun onSubmit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                expertRepository.createProfile(
                    displayName = current.displayName,
                    introduction = current.introduction,
                    careerYears = current.careerYears.toLong(),
                    mainCategoryId = requireNotNull(current.mainCategoryId),
                    mainLocationId = requireNotNull(current.mainLocationId),
                )
                _effects.emit(ExpertRegisterEffect.NavigateBack)
            } catch (e: ApiException) {
                _state.update { it.copy(errorMessage = e.message ?: "등록에 실패했어요.") }
            } catch (t: Throwable) {
                _state.update { it.copy(errorMessage = "네트워크 오류가 발생했어요.") }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
