package kyung.kung_android.ui.expert_search

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.data.expert.dto.ExpertSearchResponse
import kyung.kung_android.domain.category.model.Categories
import kyung.kung_android.domain.location.model.Regions
import kyung.kung_android.ui.theme.KungColors

@Composable
fun ExpertSearchScreen(
    initialKeyword: String? = null,
    initialCategoryId: Long? = null,
    onNavigateExpertDetail: (Long) -> Unit = {},
    viewModel: ExpertSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(initialKeyword, initialCategoryId) {
        initialKeyword?.takeIf { it.isNotEmpty() }?.let(viewModel::applyKeywordFromHome)
        initialCategoryId?.let(viewModel::applyCategoryFromHome)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBarSection(
            keyword = state.keyword,
            onKeywordChange = viewModel::onKeywordChange,
            onSubmit = viewModel::onSubmit,
        )

        FilterChipRow(
            selectedCategoryId = state.selectedCategoryId,
            selectedLocationId = state.selectedLocationId,
            onCategorySelected = viewModel::onCategorySelected,
            onLocationSelected = viewModel::onLocationSelected,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading && state.experts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.experts.isEmpty() -> {
                    EmptyState(message = state.error ?: "조건에 맞는 고수가 없어요")
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.experts, key = { it.expertProfileId }) { expert ->
                            ExpertCard(
                                expert = expert,
                                isFavorited = expert.expertProfileId in state.favoritedExpertIds,
                                onClick = { onNavigateExpertDetail(expert.expertProfileId) },
                                onFavoriteClick = { viewModel.onFavoriteToggle(expert.expertProfileId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarSection(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OutlinedTextField(
        value = keyword,
        onValueChange = onKeywordChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = { Text("어떤 서비스가 필요하세요?") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
            capitalization = KeyboardCapitalization.None,
        ),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipRow(
    selectedCategoryId: Long?,
    selectedLocationId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onLocationSelected: (Long?) -> Unit,
) {
    val selectedCategoryName = selectedCategoryId?.let(Categories::byId)?.name
    val selectedRegionName = selectedLocationId?.let(Regions::byId)?.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = { onCategorySelected(cycleCategoryId(selectedCategoryId)) },
            label = { Text(selectedCategoryName?.let { "$it ▾" } ?: "카테고리 ▾") },
        )
        AssistChip(
            onClick = { onLocationSelected(cycleLocationId(selectedLocationId)) },
            label = { Text(selectedRegionName?.let { "$it ▾" } ?: "지역 ▾") },
        )
    }
}

// 임시: 바텀시트가 도입되기 전까지 chip 한 번 누르면 다음 선택지로 순환.
// 후속 PR에서 BottomSheet 선택 UI로 교체.
private fun cycleCategoryId(current: Long?): Long? {
    val ids: List<Long?> = listOf(null) + Categories.ALL.map { it.id }
    val idx = ids.indexOf(current).let { if (it == -1) 0 else it }
    return ids[(idx + 1) % ids.size]
}

private fun cycleLocationId(current: Long?): Long? {
    val ids: List<Long?> = listOf(null) + Regions.ALL.map { it.id }
    val idx = ids.indexOf(current).let { if (it == -1) 0 else it }
    return ids[(idx + 1) % ids.size]
}

@Composable
private fun ExpertCard(
    expert: ExpertSearchResponse,
    isFavorited: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InitialAvatar(name = expert.displayName)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expert.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    if (expert.verifiedYn == "Y") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "인증",
                            tint = KungColors.Purple,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                val meta = listOfNotNull(expert.mainCategoryName, expert.mainLocationName)
                    .joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                expert.introduction?.takeIf { it.isNotBlank() }?.let { intro ->
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = intro,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                    )
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorited) "찜 해제" else "찜",
                    tint = if (isFavorited) KungColors.Error else KungColors.Gray,
                )
            }
        }
    }
}

@Composable
private fun InitialAvatar(name: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(KungColors.Purple),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.toString() ?: "?",
            color = KungColors.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
