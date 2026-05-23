package kyung.kung_android.ui.post_editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostEditorScreen(
    onBack: () -> Unit,
    viewModel: PostEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scroll = rememberScrollState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let(viewModel::onAddImage) },
    )

    LaunchedEffect(viewModel.effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    PostEditorEffect.NavigateBack -> onBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "새 글 쓰기",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = "닫기")
                    }
                },
                actions = {
                    SubmitChip(
                        enabled = state.canSubmit,
                        loading = state.isSubmitting,
                        onClick = viewModel::onSubmit,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FieldLabel("제목")
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = { Text("어떤 이야기를 나누고 싶으세요?") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = editorFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            FieldLabel("내용")
            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                placeholder = { Text("자유롭게 적어보세요.\n질문, 후기, 팁 모두 환영해요.") },
                shape = RoundedCornerShape(14.dp),
                colors = editorFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            )

            FieldLabel("사진 첨부")
            ImageAttachmentRow(
                imageFileIds = state.imageFileIds,
                uploadingCount = state.uploadingCount,
                onAdd = { pickImageLauncher.launch("image/*") },
                onRemove = viewModel::onRemoveImage,
            )

            Text(
                text = "${state.imageCount}/5",
                style = MaterialTheme.typography.labelSmall,
                color = KungColors.Hint,
            )

            state.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = KungColors.Slate,
    )
}

@Composable
private fun editorFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = KungColors.Purple,
    unfocusedBorderColor = KungColors.BorderSoft,
    focusedContainerColor = KungColors.BgRaised,
    unfocusedContainerColor = KungColors.BgSurface,
    cursorColor = KungColors.Purple,
)

@Composable
private fun SubmitChip(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    val container = if (enabled) KungColors.Purple else KungColors.BgSubtle
    val content = if (enabled) KungColors.White else KungColors.Hint
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .clip(RoundedCornerShape(50))
            .background(container)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = content,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Text(
                text = "등록",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp,
                ),
                color = content,
            )
        }
    }
}

@Composable
private fun ImageAttachmentRow(
    imageFileIds: List<Long>,
    uploadingCount: Int,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            val canAdd = imageFileIds.size + uploadingCount < 5
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(KungColors.BgSurface)
                    .border(
                        width = 1.dp,
                        color = KungColors.BorderSoft,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clickable(enabled = canAdd, onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.AddPhotoAlternate,
                        contentDescription = "이미지 추가",
                        tint = if (canAdd) KungColors.Purple else KungColors.Disabled,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "사진",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (canAdd) KungColors.Purple else KungColors.Disabled,
                    )
                }
            }
        }
        items(imageFileIds, key = { it }) { fileId ->
            Box {
                AsyncImage(
                    model = null,
                    contentDescription = null,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(KungColors.BgSubtle),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(KungColors.Charcoal.copy(alpha = 0.85f))
                        .clickable { onRemove(fileId) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "삭제",
                        tint = KungColors.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        if (uploadingCount > 0) {
            items(uploadingCount) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(KungColors.BgSubtle),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
