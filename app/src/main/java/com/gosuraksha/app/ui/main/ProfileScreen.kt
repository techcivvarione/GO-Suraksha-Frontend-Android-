package com.gosuraksha.app.ui.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gosuraksha.app.R
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.profile.ProfileViewModel
import com.gosuraksha.app.profile.model.ProfileResponse
import com.gosuraksha.app.security.SecurityViewModel
import com.gosuraksha.app.ui.components.CyberCardNew
import com.gosuraksha.app.ui.components.LanguageSwitcher
import com.gosuraksha.app.ui.components.localizedUiMessage
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Design tokens — profile screen only
// ─────────────────────────────────────────────────────────────────────────────
private object ProfileColors {
    // Hero band — always dark regardless of app theme
    val heroBg      = Color(0xFF0D1128)
    val heroSurface = Color(0xFF141A36)
    val heroOrb     = Color(0xFF00C9A7)

    // White card surface (light editorial feel)
    val cardWhite   = Color(0xFFFFFFFF)
    val cardBorder  = Color(0xFFEEF2FA)
    val cardShadow  = Color(0xFF1A2050)

    // Section header labels
    val sectionLbl  = Color(0xFF9AA5C0)

    // Row text
    val rowTitle    = Color(0xFF1A2040)
    val rowRight    = Color(0xFFB0BADB)

    // Stat ring colors
    val ringRisk    = Color(0xFFEF4444)
    val ringScans   = Color(0xFF10B981)
    val ringThreats = Color(0xFFF59E0B)
}

// ─────────────────────────────────────────────────────────────────────────────
// ProfileScreen — root
// ALL state, ViewModels, LaunchedEffects, dialogs UNCHANGED
// Only the visual layout has changed
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val viewModel: ProfileViewModel          = viewModel()
    val securityViewModel: SecurityViewModel = viewModel()

    val profile        by viewModel.profile.collectAsState()
    val loading        by viewModel.loading.collectAsState()
    val message        by viewModel.message.collectAsState()
    val securityMessage by securityViewModel.message.collectAsState()
    val imageUri       by viewModel.avatarImageUri.collectAsState()
    val user           by SessionManager.user.collectAsState()

    var showLogoutDialog   by remember { mutableStateOf(false) }
    var showDeleteDialog   by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var accountExpanded    by rememberSaveable { mutableStateOf(false) }
    var securityExpanded   by rememberSaveable { mutableStateOf(false) }

    var name           by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var currentPass    by remember { mutableStateOf("") }
    var newPass        by remember { mutableStateOf("") }
    var confirmPass    by remember { mutableStateOf("") }
    var showCurrentPass by remember { mutableStateOf(false) }
    var showNewPass     by remember { mutableStateOf(false) }
    var showConfirmPass by remember { mutableStateOf(false) }

    val planRaw = (profile?.plan?.ifBlank { "FREE" } ?: "FREE").uppercase()
    val planLabel = when (planRaw) {
        "GO_PRO" -> "GO PRO"
        "GO_ULTRA" -> "GO ULTRA"
        else -> "FREE PLAN"
    }
    val isPremium = planRaw == "GO_PRO" || planRaw == "GO_ULTRA"

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.setAvatarImageUri(uri?.toString())
    }

    LaunchedEffect(Unit)    { viewModel.loadProfile() }
    LaunchedEffect(profile) { profile?.let { name = it.name; phone = it.phone } }

    if (loading) {
        Box(
            Modifier.fillMaxSize().background(ColorTokens.background()),
            contentAlignment = Alignment.Center
        ) { EdgyLoadingAnimation() }
        return
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    // bodyBg respects dark/light mode without needing ColorTokens.LocalAppDarkMode
    val bodyBg = ColorTokens.background()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bodyBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero + score card overlap: Box makes score card sit across the seam
        Box(modifier = Modifier.fillMaxWidth()) {
            // Hero has paddingBottom=60dp so Box is tall enough for the overlap
            ProfileHeroBand(
                profile     = profile,
                fallback    = user?.name ?: stringResource(R.string.profile_guest),
                imageUri    = imageUri,
                isPremium   = isPremium,
                planLabel   = planLabel,
                planRaw     = planRaw,
                onEditPhoto = { imagePicker.launch("image/*") }
            )
            // Score card: offset(y=50dp) pushes it 50dp below hero bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .offset(y = 50.dp)
                    .fillMaxWidth()
            ) {
                ProfileScoreCard()
            }
        }

        // Spacer compensates for the card's downward offset (50dp) + card height buffer
        Spacer(Modifier.height(66.dp))

        // ── BODY ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Cyber Card (unchanged component)
            CyberCardNew(
                userName    = profile?.name?.ifBlank { user?.name }
                    ?: stringResource(R.string.profile_guest),
                cardNumber  = formatCardNumber(user?.id ?: ""),
                cyberScore  = 750,
                generatedOn = "13/02/2026",
                validTill   = "13/02/2026"
            )

            // Upgrade / Premium banner
            if (isPremium) PremiumBadge() else UpgradeBanner(onUpgrade = { viewModel.upgradeToPremium() })

            // ── Security status card ─────────────────────────────────────
            ProfileWhiteCard(header = "SECURITY STATUS") {
                ProfileStatusRow(
                    icon      = Icons.Default.Lock,
                    iconBg    = Color(0xFFFEF2F2),
                    iconTint  = ProfileColors.ringRisk,
                    title     = "Password Breach",
                    rightText = "3 found",
                    rightColor = ProfileColors.ringRisk
                )
                ProfileStatusRow(
                    icon      = Icons.Default.Shield,
                    iconBg    = Color(0xFFECFDF5),
                    iconTint  = ProfileColors.ringScans,
                    title     = "Two-Factor Auth",
                    rightText = "Active",
                    rightColor = ProfileColors.ringScans
                )
                ProfileStatusRow(
                    icon      = Icons.Default.Email,
                    iconBg    = Color(0xFFEFF6FF),
                    iconTint  = Color(0xFF3B82F6),
                    title     = "Email Monitor",
                    rightText = "Live",
                    isLast    = true
                )
            }

            // ── Account section (collapsible — unchanged logic) ───────────
            ProfileWhiteCard(header = stringResource(R.string.profile_section_title).uppercase()) {
                ProfileExpandableRow(
                    icon     = Icons.Outlined.Person,
                    title    = stringResource(R.string.profile_section_title),
                    expanded = accountExpanded,
                    onToggle = { accountExpanded = !accountExpanded },
                    isLast   = true
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        EdgyTextField(name, { name = it }, stringResource(R.string.profile_field_full_name), Icons.Outlined.Person)
                        EdgyTextField(phone, { phone = it }, stringResource(R.string.profile_field_phone), Icons.Outlined.Phone)
                        NeonButton(stringResource(R.string.profile_btn_save)) {
                            viewModel.updateProfile(name, phone)
                        }
                        message?.let { SuccessBanner(localizedUiMessage(it)) }
                    }
                }
            }

            // ── Security section (collapsible — unchanged logic) ──────────
            ProfileWhiteCard(header = stringResource(R.string.profile_section_security).uppercase()) {
                ProfileExpandableRow(
                    icon     = Icons.Outlined.Lock,
                    title    = stringResource(R.string.profile_section_security),
                    expanded = securityExpanded,
                    onToggle = { securityExpanded = !securityExpanded },
                    isLast   = true
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        EdgyTextField(
                            currentPass, { currentPass = it },
                            stringResource(R.string.profile_field_current_password),
                            Icons.Outlined.Lock,
                            visualTransformation = if (showCurrentPass) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPass = !showCurrentPass }) {
                                    Icon(
                                        if (showCurrentPass) Icons.Outlined.VisibilityOff
                                        else Icons.Outlined.Visibility,
                                        null, tint = ColorTokens.textSecondary()
                                    )
                                }
                            }
                        )
                        EdgyTextField(
                            newPass, { newPass = it },
                            stringResource(R.string.profile_field_new_password),
                            Icons.Outlined.Lock,
                            visualTransformation = if (showNewPass) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNewPass = !showNewPass }) {
                                    Icon(
                                        if (showNewPass) Icons.Outlined.VisibilityOff
                                        else Icons.Outlined.Visibility,
                                        null, tint = ColorTokens.textSecondary()
                                    )
                                }
                            }
                        )
                        if (newPass.isNotEmpty()) NeonPasswordStrength(newPass)
                        EdgyTextField(
                            confirmPass, { confirmPass = it },
                            stringResource(R.string.profile_field_confirm_password),
                            Icons.Outlined.Lock,
                            visualTransformation = if (showConfirmPass) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPass = !showConfirmPass }) {
                                    Icon(
                                        if (showConfirmPass) Icons.Outlined.VisibilityOff
                                        else Icons.Outlined.Visibility,
                                        null, tint = ColorTokens.textSecondary()
                                    )
                                }
                            }
                        )
                        NeonButton(stringResource(R.string.profile_btn_update_password)) {
                            securityViewModel.changePassword(currentPass, newPass, confirmPass)
                        }
                        securityMessage?.let { SuccessBanner(localizedUiMessage(it)) }
                    }
                }
            }

            // ── Trusted Devices ──────────────────────────────────────────
            ProfileWhiteCard(header = "TRUSTED DEVICES") {
                ProfileStatusRow(
                    icon     = Icons.Default.PhoneAndroid,
                    iconBg   = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF3B82F6),
                    title    = "This Device",
                    rightText = "Current",
                    rightColor = ProfileColors.ringScans
                )
                ProfileStatusRow(
                    icon     = Icons.Default.Laptop,
                    iconBg   = Color(0xFFF5F3FF),
                    iconTint = Color(0xFF8B5CF6),
                    title    = "Chrome on Windows",
                    rightText = "2d ago",
                    isLast   = true
                )
            }

            // ── Quick actions ────────────────────────────────────────────
            ProfileWhiteCard(header = "MORE") {
                ProfileActionRow(
                    icon = Icons.Outlined.Language,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF3B82F6),
                    title = stringResource(R.string.home_quick_language),
                    onClick = { showLanguageDialog = true }
                )
                ProfileActionRow(
                    icon = Icons.Outlined.Notifications,
                    iconBg = Color(0xFFFFFBEB),
                    iconTint = Color(0xFFF59E0B),
                    title = stringResource(R.string.profile_help_support)
                ) { /* TODO: notifications */ }
                ProfileActionRow(
                    icon = Icons.Outlined.Help,
                    iconBg = Color(0xFFF0FDF4),
                    iconTint = Color(0xFF10B981),
                    title = stringResource(R.string.profile_help_support)
                ) { /* TODO */ }
                ProfileActionRow(
                    icon = Icons.Outlined.Info,
                    iconBg = Color(0xFFF5F3FF),
                    iconTint = Color(0xFF8B5CF6),
                    title = stringResource(R.string.profile_about_us)
                ) { /* TODO */ }
                ProfileActionRow(
                    icon = Icons.Outlined.CardGiftcard,
                    iconBg = Color(0xFFFFF1F2),
                    iconTint = Color(0xFFEF4444),
                    title = stringResource(R.string.profile_refer_earn),
                    highlight = true,
                    isLast = true
                ) { /* TODO */ }
            }

            // ── Danger Zone ──────────────────────────────────────────────
            DangerZone(
                onLogoutClick = { showLogoutDialog = true },
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    }

    // ── Dialogs — ALL UNCHANGED ───────────────────────────────────────────────
    if (showLogoutDialog) {
        EdgyDialog(
            title       = stringResource(R.string.dialog_logout_title),
            message     = stringResource(R.string.dialog_logout_message),
            confirmText = stringResource(R.string.dialog_logout_confirm),
            dismissText = stringResource(R.string.dialog_logout_dismiss),
            isDanger    = true,
            onConfirm   = { securityViewModel.logoutAll(); onLogout() },
            onDismiss   = { showLogoutDialog = false }
        )
    }
    if (showDeleteDialog) {
        EdgyDialog(
            title       = stringResource(R.string.profile_delete_title),
            message     = stringResource(R.string.profile_delete_message),
            confirmText = stringResource(R.string.profile_delete_confirm),
            dismissText = stringResource(R.string.common_cancel),
            isDanger    = true,
            onConfirm   = { /* TODO: Delete account */ },
            onDismiss   = { showDeleteDialog = false }
        )
    }
    if (showLanguageDialog) {
        LanguageSwitcher(onDismiss = { showLanguageDialog = false })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO BAND — always dark, sits at the top
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeroBand(
    profile: ProfileResponse?,
    fallback: String,
    imageUri: String?,
    isPremium: Boolean,
    planLabel: String,
    planRaw: String,
    onEditPhoto: () -> Unit
) {
    val name     = profile?.name?.ifBlank { fallback } ?: fallback
    val email    = profile?.phone ?: ""          // reuse phone field for display
    val initials = name.trim().split(" ")
        .filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "U" }

    val inf = rememberInfiniteTransition(label = "hero")
    val orbScale by inf.animateFloat(
        1f, 1.18f,
        infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orb"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProfileColors.heroBg)
            .padding(top = 12.dp, bottom = 60.dp)   // bottom padding = overlap space for score card
    ) {
        // Ambient orb
        Box(
            Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .graphicsLayer { scaleX = orbScale; scaleY = orbScale }
                .background(
                    Brush.radialGradient(
                        listOf(ProfileColors.heroOrb.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Page title
            Text(
                "Profile",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00C9A7), Color(0xFF0055CC))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imageUri.isNullOrBlank()) {
                            AsyncImage(
                                model            = Uri.parse(imageUri),
                                contentDescription = null,
                                modifier         = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(22.dp)),
                                contentScale     = ContentScale.Crop
                            )
                        } else {
                            Text(
                                initials,
                                color      = Color.White,
                                fontSize   = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    // Edit button
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C9A7))
                            .clickable(onClick = onEditPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit, null,
                            tint     = Color(0xFF07090F),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        name.ifBlank { stringResource(R.string.profile_guest) },
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    if (email.isNotBlank()) {
                        Text(
                            email,
                            fontSize = 12.sp,
                            color    = Color.White.copy(alpha = 0.45f)
                        )
                    }
                    // Plan chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (planRaw) {
                                    "GO_ULTRA" -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
                                    "GO_PRO" -> Brush.horizontalGradient(listOf(Color(0xFF10B981).copy(alpha = 0.18f), Color(0xFF10B981).copy(alpha = 0.1f)))
                                    else -> Brush.horizontalGradient(listOf(Color(0xFF9CA3AF).copy(alpha = 0.2f), Color(0xFF9CA3AF).copy(alpha = 0.12f)))
                                }
                            )
                            .border(
                                1.dp,
                                when (planRaw) {
                                    "GO_ULTRA" -> Color.Transparent
                                    "GO_PRO" -> Color(0xFF10B981).copy(alpha = 0.4f)
                                    else -> Color(0xFF9CA3AF).copy(alpha = 0.4f)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "◆ $planLabel",
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color      = when (planRaw) {
                                "GO_ULTRA" -> Color.Black
                                "GO_PRO" -> Color(0xFF10B981)
                                else -> Color(0xFF9CA3AF)
                            },
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FLOATING SCORE CARD — overlaps hero/body seam
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileScoreCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 16.dp,
                shape        = RoundedCornerShape(22.dp),
                ambientColor = ProfileColors.cardShadow.copy(0.12f),
                spotColor    = ProfileColors.cardShadow.copy(0.12f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ProfileColors.cardWhite)
            .padding(vertical = 18.dp)
    ) {
        ScoreRingCell(
            value    = 18,
            max      = 100,
            label    = "Risk Score",
            color    = ProfileColors.ringRisk,
            trackCol = Color(0xFFFEE2E2)
        )
        Box(Modifier.width(1.dp).height(48.dp).background(ProfileColors.cardBorder).align(Alignment.CenterVertically))
        ScoreRingCell(
            value    = 71,
            max      = 100,
            label    = "Scans Done",
            color    = ProfileColors.ringScans,
            trackCol = Color(0xFFDCFCE7)
        )
        Box(Modifier.width(1.dp).height(48.dp).background(ProfileColors.cardBorder).align(Alignment.CenterVertically))
        ScoreRingCell(
            value    = 48,
            max      = 100,
            label    = "Threats",
            color    = ProfileColors.ringThreats,
            trackCol = Color(0xFFFEF9C3)
        )
    }
}

@Composable
private fun RowScope.ScoreRingCell(
    value: Int,
    max: Int,
    label: String,
    color: Color,
    trackCol: Color
) {
    val animatedSweep by animateFloatAsState(
        targetValue   = (value.toFloat() / max) * 360f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "ring_$label"
    )

    Column(
        modifier            = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 5f,
                    cap   = androidx.compose.ui.graphics.StrokeCap.Round
                )
                val inset  = 5f / 2f
                val rect   = androidx.compose.ui.geometry.Rect(inset, inset, size.width - inset, size.height - inset)
                // Track
                drawArc(trackCol, 0f, 360f, false, rect.topLeft, rect.size, style = stroke)
                // Progress
                drawArc(color, -90f, animatedSweep, false, rect.topLeft, rect.size, style = stroke)
            }
            Text(
                "$value",
                fontSize   = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = color
            )
        }
        Text(
            label,
            fontSize  = 9.sp,
            color     = ProfileColors.sectionLbl,
            letterSpacing = 0.3.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WHITE CARD CONTAINER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileWhiteCard(
    header: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 4.dp,
                shape        = RoundedCornerShape(18.dp),
                ambientColor = ProfileColors.cardShadow.copy(0.06f),
                spotColor    = ProfileColors.cardShadow.copy(0.06f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(ProfileColors.cardWhite)
    ) {
        Text(
            header,
            modifier      = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 8.dp, end = 16.dp),
            fontSize       = 10.sp,
            fontWeight     = FontWeight.Bold,
            color          = ProfileColors.sectionLbl,
            letterSpacing  = 1.2.sp
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(ProfileColors.cardBorder))
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ROW VARIANTS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileStatusRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    rightText: String  = "",
    rightColor: Color  = ProfileColors.rowRight,
    isLast: Boolean    = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(title, modifier = Modifier.weight(1f), fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = ProfileColors.rowTitle)
        if (rightText.isNotBlank()) {
            Text(rightText, fontSize = 12.sp, color = rightColor, fontWeight = FontWeight.SemiBold)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = ProfileColors.rowRight, modifier = Modifier.size(16.dp))
    }
    if (!isLast) Box(Modifier.fillMaxWidth().padding(start = 64.dp).height(1.dp).background(ProfileColors.cardBorder))
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    highlight: Boolean = false,
    isLast: Boolean    = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication        = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick           = onClick
            )
            .background(if (highlight) ColorTokens.accent().copy(alpha = 0.03f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(
            title,
            modifier   = Modifier.weight(1f),
            fontSize   = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color      = if (highlight) ColorTokens.accent() else ProfileColors.rowTitle
        )
        Icon(Icons.Outlined.ChevronRight, null, tint = ProfileColors.rowRight, modifier = Modifier.size(16.dp))
    }
    if (!isLast) Box(Modifier.fillMaxWidth().padding(start = 64.dp).height(1.dp).background(ProfileColors.cardBorder))
}

@Composable
private fun ProfileExpandableRow(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    isLast: Boolean = false,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(if (expanded) 90f else 0f, tween(280), label = "chev")

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication        = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    onClick           = onToggle
                )
                .background(if (expanded) ColorTokens.accent().copy(alpha = 0.03f) else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (expanded) ColorTokens.accent().copy(alpha = 0.12f)
                        else Color(0xFFEFF6FF)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint     = if (expanded) ColorTokens.accent() else Color(0xFF3B82F6),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                title,
                modifier   = Modifier.weight(1f),
                fontSize   = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color      = if (expanded) ColorTokens.accent() else ProfileColors.rowTitle
            )
            Icon(
                Icons.Outlined.ChevronRight, null,
                tint     = ProfileColors.rowRight,
                modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = rotation }
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn(tween(200)) + expandVertically(tween(280, easing = EaseOutQuart)),
            exit    = fadeOut(tween(150)) + shrinkVertically(tween(200))
        ) {
            Column(modifier = Modifier.background(Color(0xFFFAFBFF))) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(ProfileColors.cardBorder))
                content()
            }
        }

        if (!isLast) Box(Modifier.fillMaxWidth().padding(start = 64.dp).height(1.dp).background(ProfileColors.cardBorder))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ALL BELOW — UNCHANGED FROM ORIGINAL
// ─────────────────────────────────────────────────────────────────────────────

private fun formatCardNumber(id: String): String {
    val clean = id.replace("-", "").uppercase()
    return if (clean.length >= 12) {
        "${clean.substring(0,4)} ${clean.substring(4,7)} ${clean.substring(7,10)}"
    } else "CC08 K38 56B"
}

@Composable
private fun EdgyLoadingAnimation() {
    val infinite = rememberInfiniteTransition(label = "load")
    val rotation by infinite.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "spin"
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .rotate(rotation)
                .border(
                    4.dp,
                    Brush.sweepGradient(listOf(ColorTokens.accent(), Color.Transparent)),
                    CircleShape
                )
        )
    }
}

@Composable
private fun PremiumBadge() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Diamond, null, tint = Color.Black, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.profile_premium_active), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(stringResource(R.string.profile_premium_active_subtitle), fontSize = 12.sp, color = Color.Black.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun UpgradeBanner(onUpgrade: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(ColorTokens.accent().copy(0.15f), ColorTokens.accent().copy(0.05f))))
            .border(1.dp, ColorTokens.accent().copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable(onClick = onUpgrade)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Upgrade, null, tint = ColorTokens.accent(), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.profile_upgrade_title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorTokens.textPrimary())
                Text(stringResource(R.string.profile_upgrade_subtitle), fontSize = 12.sp, color = ColorTokens.textSecondary())
            }
            Icon(Icons.Default.ChevronRight, null, tint = ColorTokens.accent())
        }
    }
}

@Composable
private fun EdgyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value            = value,
        onValueChange    = onValueChange,
        label            = { Text(label) },
        leadingIcon      = { Icon(leadingIcon, null, tint = ColorTokens.accent(), modifier = Modifier.size(20.dp)) },
        trailingIcon     = trailingIcon,
        modifier         = Modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        colors           = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = ColorTokens.accent(),
            unfocusedBorderColor = ColorTokens.border(),
            focusedLabelColor    = ColorTokens.accent(),
            focusedTextColor     = ColorTokens.textPrimary(),
            unfocusedTextColor   = ColorTokens.textPrimary()
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun NeonButton(text: String, onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val alpha by pulse.animateFloat(0.6f, 1f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "glow")
    Box(
        modifier = Modifier
            .fillMaxWidth().height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(ColorTokens.accent().copy(alpha), ColorTokens.accent().copy(alpha * 0.8f))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun NeonPasswordStrength(password: String) {
    val strength = when {
        password.length >= 12 && password.any { it.isUpperCase() } && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 3
        password.length >= 8 -> 2
        else -> 1
    }
    val label = listOf("", stringResource(R.string.profile_password_weak), stringResource(R.string.profile_password_fair), stringResource(R.string.profile_password_strong), stringResource(R.string.profile_password_very_strong))[strength]
    val color = listOf(Color.Transparent, ColorTokens.error(), ColorTokens.warning(), ColorTokens.success(), ColorTokens.accent())[strength]
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i -> Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(if (i < strength) color else ColorTokens.border())) }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DangerZone(onLogoutClick: () -> Unit, onDeleteClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = Color(0xFFEF4444).copy(0.08f), spotColor = Color(0xFFEF4444).copy(0.08f))
            .clip(RoundedCornerShape(18.dp))
            .background(ProfileColors.cardWhite)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Warning, null, tint = ColorTokens.error(), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.profile_danger_zone), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ColorTokens.error(), letterSpacing = 1.sp)
        }
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTokens.error()),
            border = BorderStroke(1.dp, ColorTokens.error().copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Logout, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.profile_btn_logout_all), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTokens.error()),
            border = BorderStroke(1.dp, ColorTokens.error()),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.DeleteForever, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.profile_delete_title), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SuccessBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ColorTokens.success().copy(alpha = 0.1f))
            .border(1.dp, ColorTokens.success().copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = ColorTokens.success(), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = ColorTokens.success(), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EdgyDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ColorTokens.surface())
                .border(2.dp, if (isDanger) ColorTokens.error().copy(0.3f) else ColorTokens.border(), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column {
                if (isDanger) {
                    Icon(Icons.Default.Warning, null, tint = ColorTokens.error(), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(12.dp))
                }
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorTokens.textPrimary())
                Spacer(Modifier.height(12.dp))
                Text(message, fontSize = 14.sp, color = ColorTokens.textSecondary(), lineHeight = 20.sp)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, ColorTokens.border())) {
                        Text(dismissText, fontSize = 14.sp)
                    }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isDanger) ColorTokens.error() else ColorTokens.accent()), shape = RoundedCornerShape(12.dp)) {
                        Text(confirmText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
