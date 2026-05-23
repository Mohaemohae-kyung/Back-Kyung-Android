package kyung.kung_android.ui.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kyung.kung_android.data.community.dto.PostResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigatePostDetail: (Long) -> Unit,
    onNavigatePostWrite: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore }
            .collect { reached ->
                if (reached) viewModel.loadMore()
            }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.isLoading && state.posts.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                state.isLoading && state.posts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.posts.isEmpty() -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
                                contentAlignment = Alignment.Center,
                            ) { Text(text = state.error ?: "아직 게시글이 없어요") }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.posts, key = { it.postId }) { post ->
                            PostCard(post = post, onClick = { onNavigatePostDetail(post.postId) })
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) { CircularProgressIndicator() }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigatePostWrite,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "글쓰기")
        }
    }
}

@Composable
private fun PostCard(
    post: PostResponse,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listOfNotNull(
                        post.writerName,
                        "조회 ${post.viewCount}",
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            post.imageUrls.firstOrNull()?.let { url ->
                Spacer(modifier = Modifier.size(12.dp))
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                )
            }
        }
    }
}
