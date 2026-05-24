package kyung.kung_android.ui.expert_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kyung.kung_android.data.expert.dto.ExpertDetailResponse
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.common.SectionTitle
import kyung.kung_android.ui.common.toCareerYearLabel
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertDetailScreen(
    onBack: () -> Unit,
    onNavigateQuoteRequest: (expertId: Long, expertServiceId: Long) -> Unit,
    onNavigateLogin: () -> Unit = {},
    onNavigateEditProfile: () -> Unit = {},
    viewModel: ExpertDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.loadExpert()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "고수 상세",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (state.expert != null && !state.isMine) {
                        IconButton(onClick = viewModel::onFavoriteToggle) {
                            Icon(
                                imageVector = if (state.isFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (state.isFavorited) "찜 해제" else "찜",
                                tint = if (state.isFavorited) KungColors.Coral else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            when {
                state.expert == null -> Unit
                state.isMine -> BottomCta(
                    text = "프로필 수정하기",
                    enabled = true,
                    onClick = onNavigateEditProfile,
                )
                else -> BottomCta(
                    text = "견적 요청하기",
                    enabled = state.expert?.expertServiceIds?.isNotEmpty() == true,
                    onClick = {
                        if (!isLoggedIn) {
                            onNavigateLogin()
                            return@BottomCta
                        }
                        val firstServiceId = state.expert?.expertServiceIds?.firstOrNull() ?: return@BottomCta
                        onNavigateQuoteRequest(state.expertId, firstServiceId)
                    },
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading && state.expert == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.expert == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.error ?: "정보를 불러오지 못했어요.")
                }
            }
            else -> {
                ExpertDetailContent(
                    expert = requireNotNull(state.expert),
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ExpertDetailContent(
    expert: ExpertDetailResponse,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ProfileCard(expert = expert) }
        item { HorizontalDivider() }
        item {
            SectionTitle("소개")
            Text(
                text = expert.introduction.orEmpty().ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item { HorizontalDivider() }
        item {
            SectionTitle("활동 정보")
            InfoRow(label = "카테고리", value = expert.mainCategoryName ?: "—")
            InfoRow(label = "활동 지역", value = expert.mainLocationName ?: "—")
            InfoRow(label = "경력", value = expert.careerYears?.toCareerYearLabel() ?: "—")
        }
    }
}

@Composable
private fun ProfileCard(expert: ExpertDetailResponse) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KungColors.BorderSoft),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (expert.profileImageUrl != null) {
                AsyncImage(
                    model = expert.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(76.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                InitialAvatar(name = expert.displayName, size = 76.dp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expert.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp,
                        ),
                    )
                    if (expert.verifiedYn == "Y") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "인증",
                            tint = KungColors.Purple,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                val meta = listOfNotNull(expert.mainCategoryName, expert.mainLocationName).joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                expert.careerYears?.let {
                    Text(
                        text = "경력 ${it.toCareerYearLabel()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}


@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BottomCta(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        KungPrimaryButton(
            text = text,
            onClick = onClick,
            enabled = enabled,
        )
    }
}
