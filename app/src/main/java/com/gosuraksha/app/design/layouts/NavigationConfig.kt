package com.gosuraksha.app.design.layouts
import com.gosuraksha.app.navigation.Screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * GO SURAKSHA - NAVIGATION CONFIGURATION
 *
 * Centralized navigation structure following PhonePe/UMANG patterns.
 * Bottom navigation with 4 primary screens maximum.
 *
 * DESIGN PRINCIPLES:
 * - Maximum 4 bottom navigation items
 * - Clear icon-first navigation
 * - Always visible (no hiding)
 * - Green accent for active state
 * - All core features accessible within 2 taps
 */

object NavigationConfig {

    // ═══════════════════════════════════════════════════════════════
    // BOTTOM NAVIGATION ITEMS (MAXIMUM 4)
    // ═══════════════════════════════════════════════════════════════

    enum class BottomNavRoute(
        val route: String,
        val label: String,
        val iconFilled: ImageVector,
        val iconOutlined: ImageVector
    ) {
        HOME(
            route = "home",
            label = "Home",
            iconFilled = Icons.Filled.Home,
            iconOutlined = Icons.Outlined.Home
        ),
        SECURITY(
            route = "security",
            label = "Security",
            iconFilled = Icons.Filled.Shield,
            iconOutlined = Icons.Outlined.Shield
        ),
        ALERTS(
            route = "alerts",
            label = "Alerts",
            iconFilled = Icons.Filled.Notifications,
            iconOutlined = Icons.Outlined.Notifications
        ),
        PROFILE(
            route = "profile",
            label = "Profile",
            iconFilled = Icons.Filled.Person,
            iconOutlined = Icons.Outlined.Person
        )
    }

    val bottomNavItems = listOf(
        BottomNavRoute.HOME,
        BottomNavRoute.SECURITY,
        BottomNavRoute.ALERTS,
        BottomNavRoute.PROFILE
    )

    // ═══════════════════════════════════════════════════════════════
    // SECONDARY SCREENS (accessed from primary screens)
    // ═══════════════════════════════════════════════════════════════

    object SecondaryRoutes {
        // From Home
        const val NEWS = "news"
        const val RISK = "risk"
        val HISTORY = Screen.History.route

        // From Security
        const val SCAN = "scan"
        const val REALITY_SCAN = "reality_scan"
        const val CYBER_SOS = "cyber_sos"

        // From Alerts
        const val ALERT_DETAILS = "alert_details/{alertId}"

        // From Profile
        const val SETTINGS = "settings"
        const val LANGUAGE = "language"
        const val TRUSTED_CONTACTS = "trusted_contacts"

        // Auth
        const val LOGIN = "login"
        const val SIGNUP = "signup"
        const val ENTRY = "entry"
    }

    // ═══════════════════════════════════════════════════════════════
    // SCREEN HIERARCHY
    // ═══════════════════════════════════════════════════════════════

    /**
     * HOME SCREEN STRUCTURE:
     * ├── CyberCard (top section)
     * ├── Quick Actions (4 tiles max)
     * │   ├── Scan
     * │   ├── Reality Scan
     * │   ├── CyberSOS
     * │   └── News
     * └── Recent Activity (list)
     *
     * SECURITY SCREEN STRUCTURE:
     * ├── Security Status Card
     * ├── Health Score Card
     * ├── Risk Intelligence (link)
     * └── Audit Logs (link)
     *
     * ALERTS SCREEN STRUCTURE:
     * ├── Filter tabs (All/High/Medium/Low)
     * └── Alert list (cards)
     *
     * PROFILE SCREEN STRUCTURE:
     * ├── User info card
     * ├── Settings sections
     * │   ├── Account
     * │   ├── Security
     * │   ├── Notifications
     * │   └── Language
     * └── Sign out
     */

    // ═══════════════════════════════════════════════════════════════
    // NAVIGATION BEHAVIOR
    // ═══════════════════════════════════════════════════════════════

    object Behavior {
        // Back stack handling
        const val SAVE_STATE = true
        const val RESTORE_STATE = true

        // Bottom nav behavior
        const val SINGLE_TOP = true              // Avoid duplicate screens
        const val POP_UP_TO_START = true         // Clear back stack on tab change

        // Animation preferences
        const val USE_ANIMATIONS = true
        const val ANIMATION_DURATION_MS = 300
    }

    // ═══════════════════════════════════════════════════════════════
    // DEEP LINK SUPPORT
    // ═══════════════════════════════════════════════════════════════

    object DeepLinks {
        const val BASE_URI = "gosuraksha://app"

        // Bottom nav deep links
        const val HOME = "$BASE_URI/home"
        const val SECURITY = "$BASE_URI/security"
        const val ALERTS = "$BASE_URI/alerts"
        const val PROFILE = "$BASE_URI/profile"

        // Feature deep links
        const val CYBER_SOS = "$BASE_URI/cyber-sos"
        const val SCAN = "$BASE_URI/scan"
        const val REALITY_SCAN = "$BASE_URI/reality-scan"
        const val ALERT_DETAIL = "$BASE_URI/alert/{alertId}"
    }
}

/**
 * USAGE EXAMPLE:
 *
 * @Composable
 * fun AppNavigation() {
 *     val navController = rememberNavController()
 *
 *     Scaffold(
 *         bottomBar = {
 *             NavigationBar {
 *                 NavigationConfig.bottomNavItems.forEach { item ->
 *                     NavigationBarItem(
 *                         selected = currentRoute == item.route,
 *                         onClick = { navController.navigate(item.route) },
 *                         icon = {
 *                             Icon(
 *                                 if (selected) item.iconFilled else item.iconOutlined,
 *                                 contentDescription = item.label
 *                             )
 *                         },
 *                         label = { Text(item.label) }
 *                     )
 *                 }
 *             }
 *         }
 *     ) { padding ->
 *         NavHost(
 *             navController = navController,
 *             startDestination = NavigationConfig.BottomNavRoute.HOME.route,
 *             modifier = Modifier.padding(padding)
 *         ) {
 *             composable(NavigationConfig.BottomNavRoute.HOME.route) {
 *                 HomeScreen()
 *             }
 *             // ... other screens
 *         }
 *     }
 * }
 */