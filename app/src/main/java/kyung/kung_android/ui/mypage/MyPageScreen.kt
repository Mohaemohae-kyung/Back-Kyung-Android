package kyung.kung_android.ui.mypage

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kyung.kung_android.BuildConfig
import kyung.kung_android.data.user.dto.UserProfileResponse
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.common.LoginGate
import kyung.kung_android.ui.common.SectionTitle
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    onBack: () -> Unit,
    onNavigateLogin: () -> Unit,
    onNavigateExpertRegister: () -> Unit,
    onNavigateAccountSettings: () -> Unit = {},
    onNavigateFavorites: () -> Unit = {},
    onNavigatePaymentHistory: () -> Unit = {},
    onNavigateExpertTransactions: () -> Unit = {},
    onNavigateExpertSelf: (expertProfileId: Long?) -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(state.isLoggedIn) {
        if (state.isLoggedIn) viewModel.loadUser()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("마이페이지") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        LoginGate(
            isLoggedIn = state.isLoggedIn,
            onNavigateLogin = onNavigateLogin,
            modifier = Modifier.padding(padding),
        ) {
            LoggedInContent(
                user = state.user,
                isExpert = state.isExpert,
                modifier = Modifier.padding(padding),
                onExpertBannerClick = {
                    val id = state.user?.expertServiceId
                    if (state.isExpert && id != null) onNavigateExpertSelf(id) else onNavigateExpertRegister()
                },
                onAccountSettingsClick = onNavigateAccountSettings,
                onFavoritesClick = onNavigateFavorites,
                onPaymentHistoryClick = onNavigatePaymentHistory,
                onExpertTransactionsClick = onNavigateExpertTransactions,
            )
        }
    }
}

@Composable
private fun LoggedInContent(
    user: UserProfileResponse?,
    isExpert: Boolean,
    onExpertBannerClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onPaymentHistoryClick: () -> Unit,
    onExpertTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ExpertBanner(
                label = if (isExpert) "내 고수 프로필 보기" else "고수로 가입하기",
                onClick = onExpertBannerClick,
            )
        }

        item {
            UserCard(
                user = user,
                onAccountSettingsClick = onAccountSettingsClick,
            )
        }

        item {
            SectionTitle("거래내역")
            MyPageRow(
                title = "매칭온페이 거래내역",
                onClick = if (isExpert) onExpertTransactionsClick else onPaymentHistoryClick,
            )
        }

        item {
            SectionTitle("고수찾기")
            MyPageRow(
                title = "찜한 고수",
                onClick = onFavoritesClick,
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "앱 버전 ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun ExpertBanner(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(KungColors.ExpertGradient))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PRO",
                    color = KungColors.PurpleLight,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    color = KungColors.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KungColors.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = KungColors.White,
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: UserProfileResponse?,
    onAccountSettingsClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (user?.profileImageUrl != null) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                InitialAvatar(name = user?.name ?: "?", size = 56.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "—",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = user?.email ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = onAccountSettingsClick,
                label = { Text("계정설정") },
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun MyPageRow(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

