package kyung.kung_android.ui.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kyung.kung_android.ui.auth.login.AuthBrandHeader
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.theme.KungColors

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scroll = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    SignupEffect.NavigateBackToLogin -> onSignupSuccess()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        AuthBrandHeader()
        Spacer(Modifier.height(28.dp))
        Text(
            text = "매칭온\n계정 만들기",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
                letterSpacing = (-0.6).sp,
            ),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "간단한 정보 입력으로 시작해보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = KungColors.Gray,
        )
        Spacer(Modifier.height(20.dp))

        FieldLabel("이메일 *")
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = { Text("example@matchon.com") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = signupFieldColors(),
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        FieldLabel("비밀번호 *")
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = { Text("8~20자") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = signupFieldColors(),
            visualTransformation = PasswordVisualTransformation(),
            isError = state.passwordError != null,
            supportingText = state.passwordError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        FieldLabel("이름 *")
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            placeholder = { Text("실명을 입력해주세요") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = signupFieldColors(),
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        FieldLabel("닉네임")
        OutlinedTextField(
            value = state.nickname,
            onValueChange = viewModel::onNicknameChange,
            placeholder = { Text("커뮤니티에서 사용할 별명") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = signupFieldColors(),
            isError = state.nicknameError != null,
            supportingText = state.nicknameError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        FieldLabel("전화번호")
        OutlinedTextField(
            value = state.phone,
            onValueChange = viewModel::onPhoneChange,
            placeholder = { Text("010-1234-5678") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = signupFieldColors(),
            isError = state.phoneError != null,
            supportingText = state.phoneError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        FieldLabel("주민등록번호")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = state.residentFront,
                onValueChange = viewModel::onResidentFrontChange,
                placeholder = { Text("앞 6자리") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = signupFieldColors(),
                isError = state.residentRegistrationNumberError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) },
                ),
                modifier = Modifier.weight(1f),
            )
            Text("-", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.residentBack,
                onValueChange = viewModel::onResidentBackChange,
                placeholder = { Text("뒤 7자리") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = signupFieldColors(),
                visualTransformation = PasswordVisualTransformation(),
                isError = state.residentRegistrationNumberError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
                modifier = Modifier.weight(1f),
            )
        }
        state.residentRegistrationNumberError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        FieldLabel("상세주소")
        OutlinedTextField(
            value = state.detailAddress,
            onValueChange = viewModel::onDetailAddressChange,
            placeholder = { Text("동/호수 등 상세주소") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = signupFieldColors(),
            isError = state.detailAddressError != null,
            supportingText = state.detailAddressError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    viewModel.onSubmit()
                },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(16.dp))

        KungPrimaryButton(
            text = "가입하기",
            onClick = {
                keyboard?.hide()
                viewModel.onSubmit()
            },
            enabled = state.canSubmit,
            loading = state.isLoading,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = KungColors.Slate,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun signupFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = KungColors.Purple,
    unfocusedBorderColor = KungColors.BorderSoft,
    focusedContainerColor = KungColors.BgRaised,
    unfocusedContainerColor = KungColors.BgSurface,
    cursorColor = KungColors.Purple,
)
