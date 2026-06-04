package kyung.kung_android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kyung.kung_android.ui.theme.KungColors

private const val PIN_LENGTH = 6

/**
 * 6자리 숫자 입력 패널. 시스템 입력기를 사용하지 않고 자체 패널로 값을 받는다.
 * [resetKey] 가 바뀌면 키 배열이 다시 구성된다.
 */
@Composable
fun PinKeypad(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    resetKey: Any? = Unit,
) {
    val cells = remember(resetKey) {
        val digits = (0..9).map { it.toString() }.shuffled()
        digits.take(9) + listOf<String?>(null) + digits.drop(9) + listOf("←")
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(PIN_LENGTH) { index ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < value.length) KungColors.Purple
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        cells.chunked(3).forEach { rowCells ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowCells.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (cell == null) Modifier
                                else Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        when (cell) {
                                            "←" -> if (value.isNotEmpty()) onValueChange(value.dropLast(1))
                                            else -> if (value.length < PIN_LENGTH) onValueChange(value + cell)
                                        }
                                    }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (cell != null) {
                            Text(
                                text = cell,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}
