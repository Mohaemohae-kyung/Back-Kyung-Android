package kyung.kung_android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kyung.kung_android.ui.theme.KungColors

private const val PIN_LENGTH = 6
private const val BACKSPACE = "del"

/**
 * 6자리 숫자 입력 패널. 시스템 입력기를 사용하지 않고 자체 패널로 값을 받는다.
 *
 * 입력값 자체는 이 컴포저블이 보관하지 않는다. 화면에는 입력된 자릿수([length])만 점으로 표시하고,
 * 실제 자리값은 [onDigit]/[onDelete] 콜백으로 호출자에게만 전달한다.
 * [resetKey] 가 바뀌면 키 배열이 다시 구성된다.
 */
@Composable
fun PinKeypad(
    length: Int,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    resetKey: Any? = Unit,
) {
    val cells = remember(resetKey) {
        val digits = (0..9).map { it.toString() }.shuffled()
        digits.take(9) + listOf<String?>(null) + digits.drop(9) + listOf(BACKSPACE)
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
                        .background(if (index < length) KungColors.Purple else KungColors.BorderSoft),
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            cells.chunked(3).forEach { rowCells ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowCells.forEach { cell ->
                        KeypadCell(
                            cell = cell,
                            modifier = Modifier.weight(1f),
                            onDigit = onDigit,
                            onDelete = onDelete,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.KeypadCell(
    cell: String?,
    modifier: Modifier,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
) {
    if (cell == null) {
        Spacer(modifier.height(56.dp))
        return
    }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(if (pressed) KungColors.PurpleBg else MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (pressed) KungColors.PurpleSoft else KungColors.BorderSoft,
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                when (cell) {
                    BACKSPACE -> onDelete()
                    else -> onDigit(cell[0])
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (cell == BACKSPACE) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                contentDescription = "지우기",
                tint = if (pressed) KungColors.Purple else KungColors.Gray,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Text(
                text = cell,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (pressed) KungColors.Purple else KungColors.Ink,
            )
        }
    }
}
