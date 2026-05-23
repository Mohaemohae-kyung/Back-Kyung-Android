package kyung.kung_android.ui.common

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KungPullToRefresh(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    var pendingRefresh by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        snapshotFlow { state.distanceFraction }
            .collect { f ->
                if (f >= 1f && !pendingRefresh && !isLoading) {
                    pendingRefresh = true
                    onRefresh()
                }
            }
    }

    LaunchedEffect(pendingRefresh, isLoading) {
        if (pendingRefresh && !isLoading) {
            state.animateToHidden()
            pendingRefresh = false
        }
    }

    PullToRefreshBox(
        isRefreshing = pendingRefresh,
        state = state,
        onRefresh = {
            if (!pendingRefresh && !isLoading) {
                pendingRefresh = true
                onRefresh()
            }
        },
        modifier = modifier,
        content = content,
    )
}
