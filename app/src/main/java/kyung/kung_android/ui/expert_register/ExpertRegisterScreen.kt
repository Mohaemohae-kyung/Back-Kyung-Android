package kyung.kung_android.ui.expert_register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kyung.kung_android.domain.category.model.Categories
import kyung.kung_android.domain.location.model.Regions
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.theme.KungColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertRegisterScreen(
    onBack: () -> Unit,
    viewModel: ExpertRegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scroll = rememberScrollState()

    LaunchedEffect(viewModel.effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ExpertRegisterEffect.NavigateBack -> onBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "고수 가입",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "당신의 전문성을\n프로필로 보여주세요",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.5).sp,
                ),
            )
            Spacer(modifier = Modifier.height(6.dp))

            FieldLabel("활동명 *")
            OutlinedTextField(
                value = state.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                placeholder = { Text("예: 자소서 첨삭 고수") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = registerFieldColors(),
                isError = state.displayNameError != null,
                supportingText = state.displayNameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )

            FieldLabel("소개글 *")
            OutlinedTextField(
                value = state.introduction,
                onValueChange = viewModel::onIntroductionChange,
                placeholder = { Text("경력, 가능한 서비스, 진행 방식을 적어주세요.") },
                shape = RoundedCornerShape(14.dp),
                colors = registerFieldColors(),
                isError = state.introductionError != null,
                supportingText = state.introductionError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            )

            FieldLabel("경력 (년) *")
            OutlinedTextField(
                value = state.careerYears,
                onValueChange = viewModel::onCareerYearsChange,
                placeholder = { Text("예: 3") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = registerFieldColors(),
                isError = state.careerYearsError != null,
                supportingText = state.careerYearsError?.let { { Text(it) } },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            FieldLabel("서비스 분야 *")
            CategoryChips(
                selectedId = state.mainCategoryId,
                onSelect = viewModel::onCategorySelected,
            )

            FieldLabel("활동 지역 *")
            RegionChips(
                selectedId = state.mainLocationId,
                onSelect = viewModel::onLocationSelected,
            )

            state.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            KungPrimaryButton(
                text = "프로필 등록",
                onClick = viewModel::onSubmit,
                enabled = state.canSubmit,
                loading = state.isSubmitting,
            )

            Spacer(modifier = Modifier.height(24.dp))
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
private fun registerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = KungColors.Purple,
    unfocusedBorderColor = KungColors.BorderSoft,
    focusedContainerColor = KungColors.BgRaised,
    unfocusedContainerColor = KungColors.BgSurface,
    cursorColor = KungColors.Purple,
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Categories.ALL.forEach { category ->
            FilterChip(
                selected = selectedId == category.id,
                onClick = { onSelect(category.id) },
                label = {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KungColors.PurpleBg,
                    selectedLabelColor = KungColors.Purple,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun RegionChips(
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Regions.ALL.forEach { region ->
            FilterChip(
                selected = selectedId == region.id,
                onClick = { onSelect(region.id) },
                label = {
                    Text(
                        text = region.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KungColors.PurpleBg,
                    selectedLabelColor = KungColors.Purple,
                ),
            )
        }
    }
}
