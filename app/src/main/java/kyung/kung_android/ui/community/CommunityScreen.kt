package kyung.kung_android.ui.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kyung.kung_android.data.community.dto.PostResponse
import kyung.kung_android.data.notice.dto.NoticePostResponse
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.common.KungPullToRefresh
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigatePostDetail: (Long) -> Unit,
    onNavigateNoticeDetail: (Long) -> Unit,
    onNavigatePostWrite: () -> Unit,
    onNavigateSignup: () -> Unit,
    onNavigateExpertRegister: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifeListState = rememberLazyListState()
    val centerListState = rememberLazyListState()

    val activeListState = when (state.selectedBoard) {
        BoardType.LIFE -> lifeListState
        BoardType.CENTER -> centerListState
    }

    val shouldLoadMore by remember(state.selectedBoard) {
        derivedStateOf {
            val lastVisible = activeListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = activeListState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore, state.selectedBoard) {
        snapshotFlow { shouldLoadMore }
            .collect { reached ->
                if (reached) viewModel.loadMore()
            }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BoardTabs(
            selected = state.selectedBoard,
            onSelect = viewModel::selectBoard,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            KungPullToRefresh(
                isLoading = currentIsLoading(state),
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (state.selectedBoard) {
                    BoardType.LIFE -> LifeBoardContent(
                        listState = lifeListState,
                        posts = state.life.posts,
                        isLoading = state.life.isLoading,
                        isLoadingMore = state.life.isLoadingMore,
                        error = state.life.error,
                        onPostClick = onNavigatePostDetail,
                    )
                    BoardType.CENTER -> {
                        if (!state.canSeeCenter) {
                            CenterRestrictedNotice(
                                role = state.role,
                                onSignupClick = onNavigateSignup,
                                onExpertRegisterClick = onNavigateExpertRegister,
                            )
                        } else {
                            CenterBoardContent(
                                listState = centerListState,
                                posts = state.center.posts,
                                isLoading = state.center.isLoading,
                                isLoadingMore = state.center.isLoadingMore,
                                error = state.center.error,
                                onPostClick = onNavigateNoticeDetail,
                            )
                        }
                    }
                }
            }

            if (shouldShowWriteFab(state)) {
                ExtendedFloatingActionButton(
                    onClick = onNavigatePostWrite,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), clip = false),
                    shape = RoundedCornerShape(28.dp),
                    containerColor = KungColors.Purple,
                    contentColor = KungColors.White,
                    icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    text = {
                        Text(
                            text = "글쓰기",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    },
                )
            }
        }
    }
}

private fun currentIsLoading(state: CommunityUiState): Boolean = when (state.selectedBoard) {
    BoardType.LIFE -> state.life.isLoading
    BoardType.CENTER -> state.center.isLoading
}

private fun shouldShowWriteFab(state: CommunityUiState): Boolean = when (state.selectedBoard) {
    BoardType.LIFE -> true
    BoardType.CENTER -> false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoardTabs(
    selected: BoardType,
    onSelect: (BoardType) -> Unit,
) {
    val selectedIndex = when (selected) {
        BoardType.LIFE -> 0
        BoardType.CENTER -> 1
    }
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        indicator = { positions ->
            if (selectedIndex < positions.size) {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.then(Modifier),
                    width = 40.dp,
                    color = KungColors.Purple,
                )
            }
        },
    ) {
        Tab(
            selected = selected == BoardType.LIFE,
            onClick = { onSelect(BoardType.LIFE) },
            text = {
                Text(
                    text = "숨고생활",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (selected == BoardType.LIFE) FontWeight.Bold else FontWeight.Medium,
                    ),
                )
            },
        )
        Tab(
            selected = selected == BoardType.CENTER,
            onClick = { onSelect(BoardType.CENTER) },
            text = {
                Text(
                    text = "고수센터",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (selected == BoardType.CENTER) FontWeight.Bold else FontWeight.Medium,
                    ),
                )
            },
        )
    }
}

@Composable
private fun LifeBoardContent(
    listState: LazyListState,
    posts: List<PostResponse>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: String?,
    onPostClick: (Long) -> Unit,
) {
    when {
        isLoading && posts.isEmpty() -> CenterLoader()
        posts.isEmpty() -> EmptyState(error = error)
        else -> LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(posts, key = { it.postId }) { post ->
                PostCard(
                    post = post,
                    onClick = { onPostClick(post.postId) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            if (isLoadingMore) {
                item { BottomLoader() }
            }
        }
    }
}

@Composable
private fun CenterBoardContent(
    listState: LazyListState,
    posts: List<NoticePostResponse>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: String?,
    onPostClick: (Long) -> Unit,
) {
    when {
        isLoading && posts.isEmpty() -> CenterLoader()
        posts.isEmpty() -> EmptyState(error = error)
        else -> LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(posts, key = { it.postId }) { post ->
                NoticeCard(
                    post = post,
                    onClick = { onPostClick(post.postId) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            if (isLoadingMore) {
                item { BottomLoader() }
            }
        }
    }
}

@Composable
private fun CenterLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BottomLoader() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator() }
}

@Composable
private fun EmptyState(error: String?) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = error ?: "아직 게시글이 없어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PostCard(
    post: PostResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KungColors.BorderSoft),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp,
                            letterSpacing = (-0.2).sp,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                post.imageUrls.firstOrNull()?.let { url ->
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(KungColors.BgSubtle),
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val name = post.writerName.orEmpty().ifEmpty { "익명" }
                InitialAvatar(name = name, size = 24.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = KungColors.Slate,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(KungColors.Disabled),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = KungColors.Hint,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.viewCount.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = KungColors.Hint,
                )
            }
        }
    }
}

@Composable
private fun NoticeCard(
    post: NoticePostResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KungColors.BorderSoft),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Campaign,
                    contentDescription = null,
                    tint = KungColors.Purple,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "공지",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = KungColors.Purple,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 22.sp,
                    letterSpacing = (-0.2).sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                post.createdAt?.let { raw ->
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = KungColors.Hint,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(raw),
                        style = MaterialTheme.typography.labelMedium,
                        color = KungColors.Hint,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(KungColors.Disabled),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = KungColors.Hint,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.viewCount.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = KungColors.Hint,
                )
            }
        }
    }
}

@Composable
private fun CenterRestrictedNotice(
    role: String?,
    onSignupClick: () -> Unit,
    onExpertRegisterClick: () -> Unit,
) {
    val isLoggedIn = role != null
    val title = if (isLoggedIn) "고수센터는 고수 회원 전용이에요" else "로그인이 필요해요"
    val description = if (isLoggedIn) {
        "고수로 가입하시면 고수센터 공지를\n확인하실 수 있어요."
    } else {
        "회원가입 후 고수로 가입하시면\n고수센터 공지를 확인하실 수 있어요."
    }
    val buttonLabel = if (isLoggedIn) "고수 가입하기" else "회원가입 하기"
    val onClick = if (isLoggedIn) onExpertRegisterClick else onSignupClick

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(KungColors.PurpleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = KungColors.Purple,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KungColors.Purple,
                contentColor = KungColors.White,
            ),
        ) {
            Text(
                text = buttonLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}

private fun formatDate(raw: String): String {
    val datePart = raw.substringBefore('T')
    if (datePart.length == 10 && datePart[4] == '-' && datePart[7] == '-') {
        return datePart.replace('-', '.')
    }
    return raw
}
