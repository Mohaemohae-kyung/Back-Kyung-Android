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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import kyung.kung_android.ui.common.KungPullToRefresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kyung.kung_android.domain.category.model.Category
import kyung.kung_android.domain.location.model.Region
import kyung.kung_android.domain.location.model.Regions
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertSearchScreen(
    initialKeyword: String? = null,
    initialCategoryId: Long? = null,
    initialLocationId: Long? = null,
    onNavigateExpertDetail: (Long) -> Unit = {},
    viewModel: ExpertSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(initialKeyword, initialCategoryId, initialLocationId) {
        initialKeyword?.takeIf { it.isNotEmpty() }?.let(viewModel::applyKeywordFromHome)
        initialCategoryId?.let(viewModel::applyCategoryFromHome)
        initialLocationId?.let(viewModel::applyLocationFromHome)
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

        KungPullToRefresh(
            isLoading = state.isLoading,
            onRefresh = { viewModel.onSubmit() },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
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
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.experts, key = { it.expertServiceId }) { expert ->
                            ExpertCard(
                                expert = expert,
                                isFavorited = expert.expertProfileId in state.favoritedExpertIds,
                                onClick = { onNavigateExpertDetail(expert.expertServiceId) },
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
    var showCategorySheet by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) }

    val selectedCategoryName = selectedCategoryId?.let(Categories::byId)?.name
    val selectedRegionName = selectedLocationId?.let(Regions::byId)?.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = selectedCategoryId != null,
            onClick = {
                if (selectedCategoryId != null) onCategorySelected(null)
                else showCategorySheet = true
            },
            label = { Text(selectedCategoryName ?: "카테고리") },
            trailingIcon = {
                Icon(
                    imageVector = if (selectedCategoryId != null) Icons.Filled.Close else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (selectedCategoryId != null) "카테고리 해제" else null,
                    modifier = Modifier.size(18.dp),
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = KungColors.PurpleBg,
                selectedLabelColor = KungColors.Purple,
                selectedTrailingIconColor = KungColors.Purple,
            ),
        )
        FilterChip(
            selected = selectedLocationId != null,
            onClick = {
                if (selectedLocationId != null) onLocationSelected(null)
                else showLocationSheet = true
            },
            label = { Text(selectedRegionName ?: "지역") },
            trailingIcon = {
                Icon(
                    imageVector = if (selectedLocationId != null) Icons.Filled.Close else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (selectedLocationId != null) "지역 해제" else null,
                    modifier = Modifier.size(18.dp),
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = KungColors.PurpleBg,
                selectedLabelColor = KungColors.Purple,
                selectedTrailingIconColor = KungColors.Purple,
            ),
        )
    }

    if (showCategorySheet) {
        CategoryPickerSheet(
            selectedId = selectedCategoryId,
            onDismiss = { showCategorySheet = false },
            onSelected = { id ->
                showCategorySheet = false
                onCategorySelected(id)
            },
        )
    }
    if (showLocationSheet) {
        LocationPickerSheet(
            selectedId = selectedLocationId,
            onDismiss = { showLocationSheet = false },
            onSelected = { id ->
                showLocationSheet = false
                onLocationSelected(id)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    selectedId: Long?,
    onDismiss: () -> Unit,
    onSelected: (Long?) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        SheetTitle("카테고리 선택")
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                PickerCard(
                    label = "전체",
                    selected = selectedId == null,
                    onClick = { onSelected(null) },
                )
            }
            items(Categories.ALL, key = { it.id }) { category ->
                PickerCard(
                    label = category.name,
                    selected = selectedId == category.id,
                    onClick = { onSelected(category.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPickerSheet(
    selectedId: Long?,
    onDismiss: () -> Unit,
    onSelected: (Long?) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        SheetTitle("지역 선택")
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                PickerCard(
                    label = "전국",
                    selected = selectedId == null,
                    onClick = { onSelected(null) },
                )
            }
            items(Regions.ALL, key = { it.id }) { region ->
                PickerCard(
                    label = region.name,
                    selected = selectedId == region.id,
                    onClick = { onSelected(region.id) },
                )
            }
        }
    }
}

@Composable
private fun PickerCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (selected) KungColors.PurpleBg else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = if (selected) KungColors.Purple else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun PickerRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) KungColors.Purple else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = KungColors.Purple,
                modifier = Modifier.size(20.dp),
            )
        }
    }
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
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
