package kyung.kung_android.ui.store_editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kyung.kung_android.domain.category.model.Categories
import kyung.kung_android.domain.location.model.Regions
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.common.SectionTitle
import kyung.kung_android.ui.theme.KungColors

private val SERVICE_TYPES = listOf("ONLINE" to "온라인", "OFFLINE" to "대면", "BOTH" to "온라인/대면")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreEditorScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: StoreEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.onPickImage(uri) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is StoreEditorEffect.Success -> onSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "상품 등록",
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
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                KungPrimaryButton(
                    text = if (state.isSubmitting) "등록 중..." else "등록하기",
                    onClick = { viewModel.submit() },
                    enabled = state.canSubmit,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column {
                SectionTitle("대표 이미지")
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.thumbnailUri != null) {
                        AsyncImage(
                            model = state.thumbnailUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "대표 이미지 추가",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (state.isUploadingImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(KungColors.Charcoal.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator() }
                    }
                }
            }

            CategoryField(
                categoryId = state.categoryId,
                onSelected = viewModel::onCategorySelected,
            )

            Column {
                SectionTitle("상품명")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { Text("상품명을 입력하세요") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column {
                SectionTitle("서비스 소개")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = { Text("서비스 내용을 소개해주세요") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column {
                SectionTitle("가격")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.price,
                    onValueChange = viewModel::onPriceChange,
                    placeholder = { Text("가격 (원)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column {
                SectionTitle("진행 방식")
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SERVICE_TYPES.forEach { (value, label) ->
                        SelectChip(
                            label = label,
                            selected = state.serviceType == value,
                            onClick = { viewModel.onServiceTypeChange(value) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (state.needsLocation) {
                LocationField(
                    locationId = state.locationId,
                    onSelected = viewModel::onLocationSelected,
                )
            }

            state.error?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (state.isSubmitting) {
            Box(
                modifier = Modifier.fillMaxSize().background(KungColors.Charcoal.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun CategoryField(categoryId: Long?, onSelected: (Long) -> Unit) {
    var showSheet by remember { mutableStateOf(false) }
    Column {
        SectionTitle("카테고리")
        Spacer(modifier = Modifier.height(6.dp))
        SelectorBox(
            text = categoryId?.let { Categories.byId(it)?.name } ?: "카테고리를 선택하세요",
            isPlaceholder = categoryId == null,
            onClick = { showSheet = true },
        )
    }
    if (showSheet) {
        PickerSheet(
            title = "카테고리 선택",
            items = Categories.ALL.map { it.id to it.name },
            selectedId = categoryId,
            onDismiss = { showSheet = false },
            onSelected = { id -> showSheet = false; onSelected(id) },
        )
    }
}

@Composable
private fun LocationField(locationId: Long?, onSelected: (Long) -> Unit) {
    var showSheet by remember { mutableStateOf(false) }
    Column {
        SectionTitle("지역")
        Spacer(modifier = Modifier.height(6.dp))
        SelectorBox(
            text = locationId?.let { Regions.byId(it)?.name } ?: "지역을 선택하세요",
            isPlaceholder = locationId == null,
            onClick = { showSheet = true },
        )
    }
    if (showSheet) {
        PickerSheet(
            title = "지역 선택",
            items = Regions.ALL.map { it.id to it.name },
            selectedId = locationId,
            onDismiss = { showSheet = false },
            onSelected = { id -> showSheet = false; onSelected(id) },
        )
    }
}

@Composable
private fun SelectorBox(text: String, isPlaceholder: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color = if (selected) KungColors.PurpleBg else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerSheet(
    title: String,
    items: List<Pair<Long, String>>,
    selectedId: Long?,
    onDismiss: () -> Unit,
    onSelected: (Long) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items, key = { it.first }) { (id, name) ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSelected(id) },
                    color = if (selectedId == id) KungColors.PurpleBg else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = name,
                            color = if (selectedId == id) KungColors.Purple else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedId == id) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
