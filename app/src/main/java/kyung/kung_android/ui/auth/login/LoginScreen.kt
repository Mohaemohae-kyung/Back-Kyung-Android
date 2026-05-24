package kyung.kung_android.ui.auth.login

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kyung.kung_android.R
import kyung.kung_android.ui.common.KungPrimaryButton
import kyung.kung_android.ui.theme.KungColors

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateSignup: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.effects, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    LoginEffect.NavigateToHome -> onLoginSuccess()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(72.dp))
        AuthBrandHeader()
        Spacer(Modifier.height(36.dp))
        Text(
            text = "다시 만나서 반가워요",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "이메일로 매칭온에 로그인하세요",
            style = MaterialTheme.typography.bodyMedium,
            color = KungColors.Gray,
        )
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = { Text("이메일") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it) } },
            colors = authFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = { Text("비밀번호") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = PasswordVisualTransformation(),
            isError = state.passwordError != null,
            supportingText = state.passwordError?.let { { Text(it) } },
            colors = authFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    viewModel.onSubmit()
                },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        state.errorMessage?.let { msg ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))

        KungPrimaryButton(
            text = "로그인",
            onClick = {
                keyboard?.hide()
                viewModel.onSubmit()
            },
            enabled = state.canSubmit,
            loading = state.isLoading,
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "아직 계정이 없으신가요?",
                style = MaterialTheme.typography.bodyMedium,
                color = KungColors.Gray,
            )
            TextButton(onClick = onNavigateSignup) {
                Text(
                    text = "회원가입",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = KungColors.Purple,
                )
            }
        }
    }
}

@Composable
internal fun AuthBrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(KungColors.HeroGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_brand_logo),
                contentDescription = null,
                tint = KungColors.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = "매칭온",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp,
            ),
        )
    }
}

@Composable
internal fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = KungColors.Purple,
    unfocusedBorderColor = KungColors.BorderSoft,
    focusedContainerColor = KungColors.BgRaised,
    unfocusedContainerColor = KungColors.BgSurface,
    cursorColor = KungColors.Purple,
)
