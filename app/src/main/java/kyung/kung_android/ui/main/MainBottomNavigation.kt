package kyung.kung_android.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import kyung.kung_android.ui.navigation.AppRoute

@Composable
fun MainBottomNavigation(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
) {
    NavigationBar {
        MainTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabSelected(tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}

internal data class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

internal val MainTabs: List<MainTab> = listOf(
    MainTab(AppRoute.Tab.HOME, "홈", Icons.Filled.Home),
    MainTab(AppRoute.Tab.EXPERT_SEARCH, "고수찾기", Icons.Filled.Search),
    MainTab(AppRoute.Tab.RECEIVED_QUOTE, "받은견적", Icons.Filled.Description),
    MainTab(AppRoute.Tab.CHAT, "채팅", Icons.Filled.ChatBubble),
    MainTab(AppRoute.Tab.COMMUNITY, "커뮤니티", Icons.Filled.Group),
)
