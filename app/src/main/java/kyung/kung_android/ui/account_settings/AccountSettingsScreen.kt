package kyung.kung_android.ui.account_settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kyung.kung_android.ui.common.InitialAvatar
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    onNavigateProfileInfo: () -> Unit,
    onNavigatePasswordChange: () -> Unit,
    onNavigatePaymentPassword: () -> Unit,
    onNavigateWithdraw: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: AccountSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var nameDialog by remember { mutableStateOf(false) }
    var logoutDialog by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onPickProfileImage(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) {
                AccountSettingsEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "계정 설정",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Spacer(Modifier.height(24.dp))
            ProfileImageSection(
                imageUrl = state.user?.profileImageUrl,
                displayName = state.user?.name ?: "?",
                isUploading = state.isUploadingImage,
                onPickImage = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            )
            Spacer(Modifier.height(24.dp))
            NameRow(
                name = state.user?.name ?: "—",
                onEdit = { nameDialog = true },
            )
            HorizontalDivider()
            SettingsRow(label = "개인 정보 관리", onClick = onNavigateProfileInfo)
            HorizontalDivider()
            SettingsRow(label = "비밀번호 변경", onClick = onNavigatePasswordChange)
            HorizontalDivider()
            SettingsRow(label = "결제 비밀번호 설정", onClick = onNavigatePaymentPassword)
            HorizontalDivider()
            SettingsRow(label = "로그아웃", onClick = { logoutDialog = true })
            HorizontalDivider()
            SettingsRow(
                label = "계정 탈퇴",
                onClick = onNavigateWithdraw,
                tint = MaterialTheme.colorScheme.error,
            )
            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }

    if (nameDialog) {
        NameEditDialog(
            initial = state.user?.name.orEmpty(),
            isUpdating = state.isUpdating,
            onDismiss = { nameDialog = false },
            onConfirm = { name ->
                viewModel.updateName(name)
                nameDialog = false
            },
        )
    }

    if (logoutDialog) {
        AlertDialog(
            onDismissRequest = { logoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("정말 로그아웃하시겠어요?") },
            confirmButton = {
                TextButton(onClick = {
                    logoutDialog = false
                    viewModel.logout()
                }) { Text("로그아웃") }
            },
            dismissButton = { TextButton(onClick = { logoutDialog = false }) { Text("취소") } },
        )
    }
}

@Composable
private fun ProfileImageSection(
    imageUrl: String?,
    displayName: String,
    isUploading: Boolean,
    onPickImage: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(CircleShape),
                )
            } else {
                InitialAvatar(name = displayName, size = 96.dp)
            }
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(96.dp))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onPickImage),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "사진 변경",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun NameRow(name: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "활동명",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onEdit) { Text("수정") }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NameEditDialog(
    initial: String,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("활동명 수정") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("활동명") },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = !isUpdating && name.isNotBlank(),
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
