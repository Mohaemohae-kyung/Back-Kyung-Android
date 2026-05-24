package kyung.kung_android.ui.expert_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.domain.auth.AuthRepository
import kyung.kung_android.domain.expert.ExpertRepository
import kyung.kung_android.domain.favorite.FavoriteRepository
import kyung.kung_android.domain.user.UserRepository
import javax.inject.Inject

data class ExpertDetailUiState(
    val expertId: Long = 0L,
    val expert: ExpertDetailResponse? = null,
    val isFavorited: Boolean = false,
    val isMine: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

private data class LocalState(
    val expert: ExpertDetailResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ExpertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expertRepository: ExpertRepository,
    private val favoriteRepository: FavoriteRepository,
    userRepository: UserRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val expertId: Long = savedStateHandle.get<Long>("expertId") ?: 0L

    private val _local = MutableStateFlow(LocalState())

    val state: StateFlow<ExpertDetailUiState> = combine(
        _local,
        favoriteRepository.favoriteIds,
        userRepository.currentUser,
    ) { local, favoriteIds, me ->
        val profileId = local.expert?.expertProfileId
        val isMine = local.expert?.ownerUserId != null &&
            me?.userId != null &&
            local.expert.ownerUserId == me.userId
        ExpertDetailUiState(
            expertId = expertId,
            expert = local.expert,
            isFavorited = profileId != null && profileId in favoriteIds,
            isMine = isMine,
            isLoading = local.isLoading,
            error = local.error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ExpertDetailUiState(expertId = expertId))

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn

    fun onFavoriteToggle() {
        val profileId = _local.value.expert?.expertProfileId ?: return
        viewModelScope.launch {
            try {
                favoriteRepository.toggleFavorite(profileId)
            } catch (t: Throwable) {
                _local.update { it.copy(error = "찜 처리에 실패했어요.") }
            }
        }
    }

    fun loadExpert() {
        _local.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val expert = expertRepository.getExpertDetail(expertId)
                _local.update { it.copy(expert = expert, isLoading = false) }
            } catch (e: ApiException) {
                if (e.isAuthError) {
                    _local.update { it.copy(isLoading = false, error = null) }
                } else {
                    _local.update { it.copy(isLoading = false, error = "정보를 불러오지 못했어요.") }
                }
            } catch (t: Throwable) {
                _local.update { it.copy(isLoading = false, error = "정보를 불러오지 못했어요.") }
            }
        }
    }
}
