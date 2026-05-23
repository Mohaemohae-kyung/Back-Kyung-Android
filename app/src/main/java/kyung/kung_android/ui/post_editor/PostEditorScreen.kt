package kyung.kung_android.ui.post_editor

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
                title = { Text("글쓰기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = "닫기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::onSubmit,
                        enabled = state.canSubmit,
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        } else {
                            Text("등록")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = { Text("제목을 입력해주세요.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                placeholder = { Text("내용을 입력해주세요.") },
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )

            ImageAttachmentRow(
                imageFileIds = state.imageFileIds,
                uploadingCount = state.uploadingCount,
                onAdd = { pickImageLauncher.launch("image/*") },
                onRemove = viewModel::onRemoveImage,
            )

            state.errorMessage?.let { msg ->
                Text(text = msg, color = MaterialTheme.colorScheme.error)
            }

            Text(
                text = "📷  ${state.imageCount}/5",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))
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
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KungColors.BgSubtle)
                    .clickable(enabled = imageFileIds.size + uploadingCount < 5, onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "이미지 추가")
            }
        }
        items(imageFileIds, key = { it }) { fileId ->
            Box {
                AsyncImage(
                    model = null,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KungColors.BgSubtle),
                )
                IconButton(
                    onClick = { onRemove(fileId) },
                    modifier = Modifier.align(Alignment.TopEnd).size(40.dp),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "삭제",
                        tint = KungColors.White,
                        modifier = Modifier
                            .size(20.dp)
                            .background(KungColors.Charcoal, RoundedCornerShape(50))
                            .padding(2.dp),
                    )
                }
            }
        }
        if (uploadingCount > 0) {
            items(uploadingCount) {
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(KungColors.BgSubtle),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
