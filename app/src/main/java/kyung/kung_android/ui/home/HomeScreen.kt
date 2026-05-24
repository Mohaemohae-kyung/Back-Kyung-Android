package kyung.kung_android.ui.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.R
import kyung.kung_android.data.expert.dto.ExpertSearchResponse
import kyung.kung_android.domain.category.model.Categories
import kyung.kung_android.domain.category.model.Category
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    isExpert: Boolean = false,
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
        kyung.kung_android.ui.common.KungPullToRefresh(
            isLoading = state.isLoadingRecommended,
            onRefresh = { viewModel.loadRecommendations() },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    HomeTopBar(
                        isLoggedIn = isLoggedIn,
                        isExpert = isExpert,
                        onNavigateMyPage = onNavigateMyPage,
                        onNavigateExpertRegister = onNavigateExpertRegister,
                    )
                }
                item {
                    HomeHeroCard(
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
                .padding(end = 16.dp, bottom = 16.dp)
                .shadow(elevation = 12.dp, shape = CircleShape, clip = false),
            shape = CircleShape,
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
    isExpert: Boolean,
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(KungColors.HeroGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_brand_logo),
                contentDescription = null,
                tint = KungColors.White,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "매칭온",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
            ),
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isLoggedIn && !isExpert) {
            TextButton(
                onClick = onNavigateExpertRegister,
            ) {
                Text(
                    text = "고수가입",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KungColors.Purple,
                )
            }
        }
        IconButton(onClick = onNavigateMyPage) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "마이페이지",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeHeroCard(
    onSubmit: (String) -> Unit,
    onLocationSelected: (Long) -> Unit,
) {
    var searchText by remember { mutableStateOf("") }
    var showLocationSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(KungColors.HeroGradient))
            .padding(20.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = KungColors.White,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "원하는 고수를 빠르게 만나는 방법",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = KungColors.White,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "필요한 서비스를\n고수에게 바로 요청하세요",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 34.sp,
                    letterSpacing = (-0.6).sp,
                ),
                color = KungColors.White,
            )
            Spacer(modifier = Modifier.height(16.dp))
            HeroSearchPill(
                value = searchText,
                onValueChange = { searchText = it },
                onSubmit = { onSubmit(searchText) },
                onLocationClick = { showLocationSheet = true },
            )
        }
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

@Composable
private fun HeroSearchPill(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLocationClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "어떤 서비스가 필요하세요?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KungColors.Hint,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = KungColors.Charcoal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(KungColors.BgSurface)
                .clickable(onClick = onLocationClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = "지역 선택",
                tint = KungColors.Purple,
                modifier = Modifier.size(18.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(KungColors.HeroGradient))
                .clickable { onSubmit() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "검색",
                tint = KungColors.White,
                modifier = Modifier.size(20.dp),
            )
        }
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
                label = {
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = KungColors.BorderSoft,
                ),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = KungColors.Slate,
                ),
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
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
            ),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
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
    1L -> Icons.Filled.Work
    6L -> Icons.Filled.SelfImprovement
    11L -> Icons.Filled.School
    16L -> Icons.Filled.Brush
    else -> Icons.Filled.Apps
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = KungColors.categoryGradient(category.id)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp), clip = false, ambientColor = gradient.first(), spotColor = gradient.first())
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconForCategory(category.id),
                contentDescription = null,
                tint = KungColors.White,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = category.name.replace("/", "/\n"),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp,
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
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

    Column {
        Text(
            text = "오늘의 추천 고수",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading && experts.isEmpty()) {
            Text(
                text = "불러오는 중...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(experts, key = { it.expertServiceId }) { expert ->
                    RecommendedExpertCard(
                        expert = expert,
                        onClick = { onExpertClick(expert.expertServiceId) },
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
            .width(170.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, KungColors.BorderSoft),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InitialAvatar(name = expert.displayName, size = 60.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = expert.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            expert.mainCategoryName?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = KungColors.PurpleBg,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = KungColors.Purple,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "추천",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = KungColors.Purple,
                    )
                }
            }
        }
    }
}
