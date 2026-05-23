package kyung.kung_android.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kyung.kung_android.BuildConfig
import kyung.kung_android.domain.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.logout()
            if (BuildConfig.DEBUG && result.isFailure) {
                Log.w(TAG, "logout server call failed: ${result.exceptionOrNull()?.javaClass?.simpleName}")
            }
            onDone()
        }
    }

    private companion object {
        private const val TAG = "HomeViewModel"
    }
}
