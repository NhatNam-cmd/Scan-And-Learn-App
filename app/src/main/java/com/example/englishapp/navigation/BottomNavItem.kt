package com.example.englishapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem(
        route = Routes.DASHBOARD,
        title = "Home",
        icon = Icons.Default.Home
    )

    data object Scan : BottomNavItem(
        route = Routes.SCAN,
        title = "Scan",
        icon = Icons.Default.CameraAlt
    )

    data object Vocabulary : BottomNavItem(
        route = Routes.VOCABULARY,
        title = "Vocabulary",
        icon = Icons.Default.MenuBook
    )

    data object StoryHome : BottomNavItem(
        route = Routes.STORY_HOME,
        title = "Story",
        icon = Icons.Default.AutoAwesome
    )

    data object Profile : BottomNavItem(
        route = Routes.PROFILE,
        title = "Profile",
        icon = Icons.Default.Person
    )
}