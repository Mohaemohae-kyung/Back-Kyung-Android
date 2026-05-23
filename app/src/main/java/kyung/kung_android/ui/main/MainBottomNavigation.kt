package kyung.kung_android.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kyung.kung_android.ui.navigation.AppRoute
import kyung.kung_android.ui.theme.KungColors

@Composable
fun MainBottomNavigation(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    ) {
        MainTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KungColors.Purple,
                    selectedTextColor = KungColors.Purple,
                    indicatorColor = KungColors.PurpleBg,
                    unselectedIconColor = KungColors.Hint,
                    unselectedTextColor = KungColors.Hint,
                ),
            )
        }
    }
}

internal data class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

internal val MainTabs: List<MainTab> = listOf(
    MainTab(AppRoute.Tab.HOME, "홈", Icons.Outlined.Home, Icons.Filled.Home),
    MainTab(AppRoute.Tab.EXPERT_SEARCH, "고수찾기", Icons.Outlined.Search, Icons.Filled.Search),
    MainTab(AppRoute.Tab.RECEIVED_QUOTE, "받은견적", Icons.Outlined.Description, Icons.Filled.Description),
    MainTab(AppRoute.Tab.CHAT, "채팅", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
    MainTab(AppRoute.Tab.COMMUNITY, "커뮤니티", Icons.Outlined.Group, Icons.Filled.Group),
)
