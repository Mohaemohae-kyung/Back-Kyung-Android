package kyung.kung_android.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
) : ViewModel() {
    // 추천 고수 API 연동·검색·지역 선택 상태는 후속 PR에서 추가.
}
