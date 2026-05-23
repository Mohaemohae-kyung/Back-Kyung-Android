package kyung.kung_android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.data.expert.dto.ExpertSearchResponse
import kyung.kung_android.domain.category.model.Categories
import kyung.kung_android.domain.category.model.Category
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    onNavigateLogin: () -> Unit,
    onNavigateMyPage: () -> Unit,
    onNavigateExpertSearch: (keyword: String?, categoryId: Long?, locationId: Long?) -> Unit,
    onNavigateExpertDetail: (Long) -> Unit,
    onNavigateChatbot: () -> Unit,
    onNavigateExpertRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.loadRecommendations()
        onPauseOrDispose { }
    }

    Box(modifier = modifier.fillMaxSize()) {
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = state.isLoadingRecommended && state.recommended.isNotEmpty(),
            onRefresh = { viewModel.loadRecommendations() },
            modifier = Modifier.fillMaxSize(),
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                HomeTopBar(
                    isLoggedIn = isLoggedIn,
                    onNavigateMyPage = onNavigateMyPage,
                    onNavigateExpertRegister = onNavigateExpertRegister,
                )
            }
            item { HomeHeroCopy() }
            item {
                HomeSearchSection(
                    onSubmit = { keyword -> onNavigateExpertSearch(keyword.takeIf { it.isNotEmpty() }, null, null) },
                    onLocationSelected = { locationId -> onNavigateExpertSearch(null, null, locationId) },
                )
            }
            item {
                HomeQuickTags(
                    onTagClick = { keyword -> onNavigateExpertSearch(keyword, null, null) },
                )
            }
            item {
                HomeCategoryGrid(
                    onCategoryClick = { category -> onNavigateExpertSearch(null, category.id, null) },
                )
            }
            item {
                HomeRecommendedExpertsSection(
                    experts = state.recommended,
                    isLoading = state.isLoadingRecommended,
                    onExpertClick = onNavigateExpertDetail,
                )
            }
        }
        }

        FloatingActionButton(
            onClick = onNavigateChatbot,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            containerColor = KungColors.Charcoal,
            contentColor = KungColors.White,
        ) {
            Icon(
                imageVector = Icons.Filled.HeadsetMic,
                contentDescription = "챗봇 상담",
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    isLoggedIn: Boolean,
    onNavigateMyPage: () -> Unit,
    onNavigateExpertRegister: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(listOf(KungColors.Purple, KungColors.PurpleLight))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = KungColors.White,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "매칭온",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onNavigateMyPage) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "마이페이지",
            )
        }

        if (isLoggedIn) {
            Button(
                onClick = onNavigateExpertRegister,
                modifier = Modifier.height(36.dp),
            ) {
                Text("고수가입")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSearchSection(
    onSubmit: (String) -> Unit,
    onLocationSelected: (Long) -> Unit,
) {
    var searchText by remember { mutableStateOf("") }
    var showLocationSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("어떤 서비스가 필요하세요?") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit(searchText) }),
        )
        AssistChip(
            onClick = { showLocationSheet = true },
            label = { Text("지역 ▾") },
        )
    }

    if (showLocationSheet) {
        LocationPickerSheet(
            onDismiss = { showLocationSheet = false },
            onSelected = { region ->
                showLocationSheet = false
                onLocationSelected(region.id)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPickerSheet(
    onDismiss: () -> Unit,
    onSelected: (kyung.kung_android.domain.location.model.Region) -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = "지역 선택",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn {
                items(
                    kyung.kung_android.domain.location.model.Regions.ALL,
                    key = { it.id },
                ) { region ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(region) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(text = region.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun HomeHeroCopy() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(KungColors.PurpleBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = KungColors.Purple,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "원하는 고수를 빠르게 만나는 방법",
                style = MaterialTheme.typography.labelMedium,
                color = KungColors.Purple,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "필요한 서비스를\n고수에게 바로 요청하세요",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "분야와 지역을 선택하고, 마음에 드는 고수에게 견적을 요청해보세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HomeQuickTags(
    onTagClick: (String) -> Unit,
) {
    val tags = remember {
        listOf("자소서 첨삭", "코딩 과외", "로고 디자인", "번역", "생활 도움")
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tags) { tag ->
            AssistChip(
                onClick = { onTagClick(tag) },
                label = { Text(tag) },
            )
        }
    }
}

@Composable
private fun HomeCategoryGrid(
    onCategoryClick: (Category) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "분야별 고수 찾기",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Categories.ALL.forEach { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun iconForCategory(id: Long) = when (id) {
    1L -> Icons.Filled.Work             // 취업/직무
    6L -> Icons.Filled.SelfImprovement  // 취미/자기계발
    11L -> Icons.Filled.School          // 과외
    16L -> Icons.Filled.Brush           // 외주
    else -> Icons.Filled.Apps           // 기타
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(KungColors.PurpleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconForCategory(category.id),
                contentDescription = null,
                tint = KungColors.Purple,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun HomeRecommendedExpertsSection(
    experts: List<ExpertSearchResponse>,
    isLoading: Boolean,
    onExpertClick: (Long) -> Unit,
) {
    if (!isLoading && experts.isEmpty()) return

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
        Text(
            text = "오늘의 추천 고수",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading && experts.isEmpty()) {
            Text(
                text = "불러오는 중...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp),
            ) {
                items(experts, key = { it.expertProfileId }) { expert ->
                    RecommendedExpertCard(
                        expert = expert,
                        onClick = { onExpertClick(expert.expertProfileId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedExpertCard(
    expert: ExpertSearchResponse,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(KungColors.Purple),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = expert.displayName.firstOrNull()?.toString() ?: "?",
                    color = KungColors.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = expert.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            expert.mainCategoryName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

