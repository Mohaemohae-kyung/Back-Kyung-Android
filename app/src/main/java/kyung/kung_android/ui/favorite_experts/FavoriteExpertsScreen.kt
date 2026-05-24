package kyung.kung_android.ui.favorite_experts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import kyung.kung_android.ui.common.KungPullToRefresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.data.favorite.dto.FavoriteExpertResponse
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.common.toCareerYearLabel
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteExpertsScreen(
    onBack: () -> Unit,
    onNavigateExpertDetail: (Long) -> Unit,
    onNavigateExpertSearch: () -> Unit,
    viewModel: FavoriteExpertsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "찜한 고수",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        KungPullToRefresh(
            isLoading = state.isLoading,
            onRefresh = { viewModel.load() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        when {
            state.isLoading && state.favorites.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.favorites.isEmpty() -> EmptyState(
                modifier = Modifier.fillMaxSize(),
                onNavigateExpertSearch = onNavigateExpertSearch,
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.favorites, key = { it.expertProfileId }) { fav ->
                    FavoriteCard(
                        item = fav,
                        onClick = { onNavigateExpertDetail(fav.expertProfileId) },
                        onToggle = { viewModel.onToggleFavorite(fav.expertProfileId) },
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun FavoriteCard(
    item: FavoriteExpertResponse,
    onClick: () -> Unit,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KungColors.BorderSoft),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (item.profileImageUrl != null) {
                AsyncImage(
                    model = item.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                InitialAvatar(name = item.displayName, size = 52.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                val sub = buildString {
                    item.mainCategoryName?.let { append(it) }
                    if (item.mainCategoryName != null && item.careerYears != null) append(" · ")
                    item.careerYears?.let { append("경력 ${it.toCareerYearLabel()}") }
                }
                if (sub.isNotEmpty()) {
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "찜 해제",
                    tint = KungColors.Coral,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onNavigateExpertSearch: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(KungColors.PurpleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = KungColors.Purple,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "아직 찜한 고수가 없어요",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "마음에 드는 고수를 찜하고 빠르게 찾아보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = KungColors.Gray,
        )
        Spacer(Modifier.height(20.dp))
        KungPrimaryButton(
            text = "고수 찾으러 가기",
            onClick = onNavigateExpertSearch,
        )
    }
}
