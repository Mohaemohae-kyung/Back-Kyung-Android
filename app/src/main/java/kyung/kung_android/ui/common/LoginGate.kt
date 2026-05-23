package kyung.kung_android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kyung.kung_android.ui.theme.KungColors

@Composable
fun LoginGate(
    isLoggedIn: Boolean,
    onNavigateLogin: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (isLoggedIn) {
        content()
    } else {
        LoginPromptScreen(
            onNavigateLogin = onNavigateLogin,
            modifier = modifier,
        )
    }
}

@Composable
private fun LoginPromptScreen(
    onNavigateLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(KungColors.Purple),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = KungColors.White,
                modifier = Modifier.size(56.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "매칭온",
            style = MaterialTheme.typography.titleLarge,
            color = KungColors.Purple,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "매칭온 서비스를 이용하려면\n로그인이 필요합니다",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateLogin,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("로그인")
        }
    }
}
