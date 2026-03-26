package com.gosuraksha.app.ui.main

// =============================================================================
// DrawerMenu.kt — Style E redesign (dark + light compatible)
//
// Design:
//   • Centered large avatar (72dp) + name + phone
//   • 3 stat mini-cards: Score · Threats · Scans
//   • Plan badge below name
//   • Active route highlighted with colored bg + accent text
//   • Sections: Scam Detection / Threat Intelligence / Account & Safety
//   • Upgrade to PRO banner pinned at bottom
//   • Back (←) button top-right to close
//   • No extra green in dark mode — purple/indigo accent system
//   • Full dark + light compatibility via isDark flag
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gosuraksha.app.navigation.Screen

// ── Colour tokens (local to drawer, no dependency on GS/ColorTokens) ─────────
private fun bg(d: Boolean)           = if (d) Color(0xFF0F0F1A) else Color(0xFFFFFFFF)
private fun surface(d: Boolean)      = if (d) Color(0xFF1A1A2E) else Color(0xFFF5F5F7)
private fun surfaceCard(d: Boolean)  = if (d) Color(0xFF16123A) else Color(0xFFEEF2FF)
private fun border(d: Boolean)       = if (d) Color(0xFF2A2A3E) else Color(0xFFE5E7EB)
private fun textPri(d: Boolean)      = if (d) Color(0xFFFFFFFF) else Color(0xFF111111)
private fun textSec(d: Boolean)      = if (d) Color(0xFF9CA3B8) else Color(0xFF6B7280)
private fun textMuted(d: Boolean)    = if (d) Color(0xFF3A3A5A) else Color(0xFFD1D5DB)
private fun activeItemBg(d: Boolean) = if (d) Color(0xFF16123A) else Color(0xFFEEF2FF)
private fun activeItemBorder(d: Boolean) = if (d) Color(0xFF2D1F6E) else Color(0xFFC7D2FE)
private val AccentPurple             = Color(0xFF7C3AED)
private val AccentPurpleLight        = Color(0xFF6D28D9)
private fun accent(d: Boolean)       = if (d) AccentPurple else AccentPurpleLight
private val StatRed                  = Color(0xFFF87171)
private val StatBlue                 = Color(0xFF60A5FA)
private val StatGreen                = Color(0xFF34D399)

// Icon bg pairs
private fun iconBgRed(d: Boolean)    = if (d) Color(0xFF3D1515) else Color(0xFFFEF2F2)
private fun iconBgAmber(d: Boolean)  = if (d) Color(0xFF3D2A00) else Color(0xFFFFFBEB)
private fun iconBgBlue(d: Boolean)   = if (d) Color(0xFF1E3A5F) else Color(0xFFEFF6FF)
private fun iconBgPurple(d: Boolean) = if (d) Color(0xFF2D1F6E) else Color(0xFFF5F3FF)
private fun iconBgGray(d: Boolean)   = if (d) Color(0xFF1A1A2E) else Color(0xFFF3F4F6)

private fun iconTintRed(d: Boolean)    = if (d) Color(0xFFF87171) else Color(0xFFEF4444)
private fun iconTintAmber(d: Boolean)  = if (d) Color(0xFFFBBF24) else Color(0xFFD97706)
private fun iconTintBlue(d: Boolean)   = if (d) Color(0xFF60A5FA) else Color(0xFF2563EB)
private fun iconTintPurple(d: Boolean) = if (d) Color(0xFFA78BFA) else Color(0xFF7C3AED)
private fun iconTintGray(d: Boolean)   = if (d) Color(0xFF9CA3AF) else Color(0xFF6B7280)

// ── Data ──────────────────────────────────────────────────────────────────────
private data class DrawerItem(
    val route:      String,
    val label:      String,
    val icon:       ImageVector,
    val iconBgDark: Color,
    val iconBgLight:Color,
    val tintDark:   Color,
    val tintLight:  Color
)

private data class DrawerSection(
    val title: String,
    val items: List<DrawerItem>
)

@Composable
fun DrawerMenu(
    isDark:          Boolean,
    currentRoute:    String?,
    userName:        String,
    phoneNumber:     String,
    profileImageUrl: String?,
    onProfileClick:  () -> Unit,
    onNavigate:      (String) -> Unit,
    onClose:         () -> Unit = {},
    plan:            String    = "FREE",    // e.g. "FREE", "GO_PRO", "GO_ULTRA"
    cyberScore:      Int       = 0,
    threatCount:     Int       = 0,
    scanCount:       Int       = 0,
) {
    val context = LocalContext.current

    val initials = userName.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "G" }

    val sections = listOf(
        DrawerSection("Scam Detection", listOf(
            DrawerItem(Screen.Scan.route,         "QR & Threat Scan",     Icons.Rounded.QrCode,
                iconBgRed(true), iconBgRed(false), iconTintRed(true), iconTintRed(false)),
            DrawerItem(Screen.CheckNumber.route,  "Check Phone Number",   Icons.Rounded.Search,
                iconBgBlue(true), iconBgBlue(false), iconTintBlue(true), iconTintBlue(false)),
            DrawerItem(Screen.ReportScam.route,   "Report Fraud",         Icons.Rounded.Report,
                iconBgRed(true), iconBgRed(false), iconTintRed(true), iconTintRed(false)),
            DrawerItem(Screen.Scan.route,         "Password Leak Check",  Icons.Rounded.Lock,
                iconBgAmber(true), iconBgAmber(false), iconTintAmber(true), iconTintAmber(false)),
            DrawerItem(Screen.Scan.route,         "Email Breach Check",   Icons.Rounded.Email,
                iconBgPurple(true), iconBgPurple(false), iconTintPurple(true), iconTintPurple(false))
        )),
        DrawerSection("Threat Intelligence", listOf(
            DrawerItem(Screen.ScamAlertHub.route,    "Scam Network Hub",  Icons.Rounded.Hub,
                iconBgAmber(true), iconBgAmber(false), iconTintAmber(true), iconTintAmber(false)),
            DrawerItem(Screen.ScamAlertsFeed.route,  "Scam Alerts Feed",  Icons.Rounded.Notifications,
                iconBgRed(true), iconBgRed(false), iconTintRed(true), iconTintRed(false)),
            DrawerItem(Screen.Alerts.route,          "Danger Alerts",     Icons.Rounded.Warning,
                iconBgRed(true), iconBgRed(false), iconTintRed(true), iconTintRed(false)),
            DrawerItem(Screen.RiskInternal.route,    "Risk Intelligence", Icons.Rounded.Star,
                iconBgAmber(true), iconBgAmber(false), iconTintAmber(true), iconTintAmber(false)),
            DrawerItem(Screen.News.route,            "Cyber News",        Icons.AutoMirrored.Rounded.Article,
                iconBgBlue(true), iconBgBlue(false), iconTintBlue(true), iconTintBlue(false))
        )),
        DrawerSection("Account & Safety", listOf(
            DrawerItem(Screen.CyberCard.route,       "Cyber Card",        Icons.Rounded.CreditCard,
                iconBgPurple(true), iconBgPurple(false), iconTintPurple(true), iconTintPurple(false)),
            DrawerItem(Screen.TrustedContacts.route, "Trusted Circle",    Icons.Rounded.FamilyRestroom,
                iconBgBlue(true), iconBgBlue(false), iconTintBlue(true), iconTintBlue(false)),
            DrawerItem(Screen.History.route,         "Scan History",      Icons.Rounded.History,
                iconBgGray(true), iconBgGray(false), iconTintGray(true), iconTintGray(false)),
            DrawerItem(Screen.CyberSos.route,        "Cyber SOS",         Icons.Rounded.LocalHospital,
                iconBgRed(true), iconBgRed(false), iconTintRed(true), iconTintRed(false)),
            DrawerItem(Screen.Profile.route,         "Profile & Settings",Icons.Rounded.Settings,
                iconBgGray(true), iconBgGray(false), iconTintGray(true), iconTintGray(false))
        ))
    )

    ModalDrawerSheet(
        drawerContainerColor = bg(isDark),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── Top bar: App name + close button ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = "GO Suraksha",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = textPri(isDark),
                    letterSpacing = (-0.3).sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(surface(isDark))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onClose
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Close menu",
                        tint               = textSec(isDark),
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            // ── Profile block ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(surface(isDark))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onProfileClick
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(border(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(profileImageUrl)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier     = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text       = initials,
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = textPri(isDark)
                        )
                    }
                }

                // Name
                Text(
                    text       = userName,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = textPri(isDark),
                    letterSpacing = (-0.3).sp
                )

                // Phone
                Text(
                    text     = phoneNumber,
                    fontSize = 11.sp,
                    color    = textSec(isDark)
                )

                // Plan badge — always derived from the plan param, never hardcoded
                val planUpper    = plan.uppercase()
                val planBadgeLabel = when (planUpper) { "GO_ULTRA" -> "✦  GO ULTRA"; "GO_PRO" -> "★  GO PRO"; else -> "◆  FREE" }
                val planBadgeBg    = when (planUpper) {
                    "GO_ULTRA" -> if (isDark) Color(0xFF3D1F6E) else Color(0xFFF5F3FF)
                    "GO_PRO"   -> if (isDark) Color(0xFF1A3A5C) else Color(0xFFEFF6FF)
                    else       -> if (isDark) Color(0xFF1A1A2E) else Color(0xFFF3F4F6)
                }
                val planBadgeText  = when (planUpper) {
                    "GO_ULTRA" -> Color(0xFFD8B4FE)
                    "GO_PRO"   -> Color(0xFF93C5FD)
                    else       -> accent(isDark)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(planBadgeBg)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = planBadgeLabel,
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color      = planBadgeText
                    )
                }

                // Stats row — uses live data from caller, not hardcoded values
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    DrawerStat(cyberScore.toString(), "Score",   textPri(isDark), isDark)
                    Box(Modifier.width(0.5.dp).height(28.dp).background(border(isDark)))
                    DrawerStat(threatCount.toString(), "Threats", StatRed,        isDark)
                    Box(Modifier.width(0.5.dp).height(28.dp).background(border(isDark)))
                    DrawerStat(scanCount.toString(),  "Scans",   StatBlue,        isDark)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Home shortcut ─────────────────────────────────────────────
            DrawerNavItem(
                item = DrawerItem(
                    route      = Screen.Home.route,
                    label      = "Home",
                    icon       = Icons.Rounded.Home,
                    iconBgDark = iconBgGray(true),
                    iconBgLight= iconBgGray(false),
                    tintDark   = iconTintGray(true),
                    tintLight  = iconTintGray(false)
                ),
                isActive     = currentRoute == Screen.Home.route,
                isDark       = isDark,
                onClick      = { onNavigate(Screen.Home.route) }
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(0.5.dp)
                    .background(border(isDark))
            )

            // ── Sections ──────────────────────────────────────────────────
            sections.forEach { section ->
                Text(
                    text          = section.title.uppercase(),
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = textMuted(isDark),
                    letterSpacing = 0.9.sp,
                    modifier      = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 2.dp)
                )
                section.items.forEach { item ->
                    DrawerNavItem(
                        item      = item,
                        isActive  = currentRoute == item.route,
                        isDark    = isDark,
                        onClick   = { onNavigate(item.route) }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(0.5.dp)
                        .background(border(isDark))
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Upgrade banner (hidden for ULTRA) ────────────────────────
            val planUpperBanner = plan.uppercase()
            if (planUpperBanner != "GO_ULTRA") {
                val upgradeTo   = if (planUpperBanner == "GO_PRO") "GO ULTRA" else "GO PRO"
                val upgradeSub  = if (planUpperBanner == "GO_PRO")
                    "Family safety · Unlimited alerts"
                else
                    "Unlimited scans · Dark web alerts"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceCard(isDark))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDark) Color(0xFF2D1F6E) else Color(0xFFEDE9FE)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Star,
                                contentDescription = null,
                                tint               = accent(isDark),
                                modifier           = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text       = "Upgrade to $upgradeTo",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = textPri(isDark)
                            )
                            Text(
                                text     = upgradeSub,
                                fontSize = 10.sp,
                                color    = textSec(isDark)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(accent(isDark))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = { onNavigate(Screen.Profile.route) }
                            )
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "Upgrade Now  →",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// =============================================================================
// DrawerNavItem — active route highlighted
// =============================================================================
@Composable
private fun DrawerNavItem(
    item:     DrawerItem,
    isActive: Boolean,
    isDark:   Boolean,
    onClick:  () -> Unit
) {
    val iconBg   = if (isDark) item.iconBgDark  else item.iconBgLight
    val iconTint = if (isDark) item.tintDark    else item.tintLight
    val activeBg = activeItemBg(isDark)
    val accentClr = accent(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isActive) Modifier
                    .background(activeBg)
                    .then(
                        Modifier.padding(0.dp) // border effect via bg only
                    )
                else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active indicator bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isActive) accentClr else Color.Transparent)
        )

        // Icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isActive) {
                    if (isDark) Color(0xFF2D1F6E) else Color(0xFFDDD6FE)
                } else iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = item.label,
                tint               = if (isActive) accentClr else iconTint,
                modifier           = Modifier.size(15.dp)
            )
        }

        // Label
        Text(
            text       = item.label,
            fontSize   = 13.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isActive) accentClr else textSec(isDark)
        )
    }
}

// =============================================================================
// DrawerStat
// =============================================================================
@Composable
private fun DrawerStat(value: String, label: String, valueColor: Color, isDark: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text       = value,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            color      = valueColor
        )
        Text(
            text     = label,
            fontSize = 9.sp,
            color    = textMuted(isDark)
        )
    }
}