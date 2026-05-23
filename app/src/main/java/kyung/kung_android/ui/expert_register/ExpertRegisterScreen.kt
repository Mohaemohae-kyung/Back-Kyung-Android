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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kyung.kung_android.domain.category.model.Categories
import kyung.kung_android.domain.location.model.Regions

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
                title = { Text("고수 가입") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = { Text("활동명 *") },
                singleLine = true,
                isError = state.displayNameError != null,
                supportingText = state.displayNameError?.let { { Text(it) } },
                placeholder = { Text("예: 자소서 첨삭 고수") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.introduction,
                onValueChange = viewModel::onIntroductionChange,
                label = { Text("소개글 *") },
                isError = state.introductionError != null,
                supportingText = state.introductionError?.let { { Text(it) } },
                placeholder = { Text("경력, 가능한 서비스, 진행 방식을 적어주세요.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            )

            OutlinedTextField(
                value = state.careerYears,
                onValueChange = viewModel::onCareerYearsChange,
                label = { Text("경력 (년) *") },
                singleLine = true,
                isError = state.careerYearsError != null,
                supportingText = state.careerYearsError?.let { { Text(it) } },
                placeholder = { Text("예: 3") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("서비스 분야 *", style = MaterialTheme.typography.titleSmall)
            CategoryChips(
                selectedId = state.mainCategoryId,
                onSelect = viewModel::onCategorySelected,
            )

            Text("활동 지역 *", style = MaterialTheme.typography.titleSmall)
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

            Button(
                onClick = viewModel::onSubmit,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(20.dp),
                    )
                } else {
                    Text("프로필 등록")
                }
            }
        }
    }
}

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
                label = { Text(category.name) },
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
                label = { Text(region.name) },
            )
        }
    }
}
