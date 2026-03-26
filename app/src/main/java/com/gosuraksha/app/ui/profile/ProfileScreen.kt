package com.gosuraksha.app.ui.main

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gosuraksha.app.R
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.domain.model.Feature
import com.gosuraksha.app.domain.model.hasFeature
import com.gosuraksha.app.network.QuotaResponse
import com.gosuraksha.app.profile.ProfileViewModel
import com.gosuraksha.app.profile.ProfileViewModelFactory
import com.gosuraksha.app.security.model.SecurityViewModel
import com.gosuraksha.app.security.model.SecurityViewModelFactory
import com.gosuraksha.app.ui.components.localizedUiMessage

// =============================================================================
// ProfileScreen — Premium fintech-grade profile (GO Suraksha)
// Design: PhonePe / CRED / Google Pay aesthetic
//
// Layout:
//   Hero (greeting + avatar + plan badge)
//   → Security Status card (score + progress + stats)
//   → Scan Usage card (quota display)
//   → ULTRA Family card (ULTRA users only)
//   → Upgrade card (FREE → PRO | PRO → ULTRA | hidden for ULTRA)
//   → Cyber Card (feature-gated by Feature.CYBER_CARD)
//   → Quick Actions 2×2 grid
//   → Account Settings section
//   → Security & Preferences section
// =============================================================================

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val app = LocalContext.current.applicationContext as android.app.Application
    val viewModel: ProfileViewModel     = viewModel(factory = ProfileViewModelFactory(app))
    val securityViewModel: SecurityViewModel = viewModel(factory = SecurityViewModelFactory(app))
    val cyberCardViewModel: CyberCardViewModel = viewModel(factory = CyberCardViewModelFactory(app))

    val profileState    by viewModel.profile.collectAsStateWithLifecycle()
    val loading         by viewModel.loading.collectAsStateWithLifecycle()
    val message         by viewModel.message.collectAsStateWithLifecycle()
    val securityMessage by securityViewModel.message.collectAsStateWithLifecycle()
    val imageUri        by viewModel.avatarImageUri.collectAsStateWithLifecycle()
    val isDeleting      by viewModel.isDeleting.collectAsStateWithLifecycle()
    val user            by SessionManager.user.collectAsStateWithLifecycle()
    val cyberCard       by cyberCardViewModel.card.collectAsStateWithLifecycle()
    val quota           by viewModel.quota.collectAsStateWithLifecycle()

    val context  = LocalContext.current
    val activity = context as? Activity
    val isDark   = ColorTokens.LocalAppDarkMode.current

    var dialogs   by remember { mutableStateOf(ProfileDialogUiState()) }
    var formState by remember { mutableStateOf(ProfileFormUiState()) }
    var contentVisible by remember { mutableStateOf(false) }

    val profile             = profileState
    val currentUserName     = user?.name?.trim().orEmpty()
    val deleteInput         = formState.deleteConfirmationInput
    val isDeleteValid       = deleteInput.trim().isNotEmpty() && deleteInput.trim() == currentUserName

    // ── Plan resolution (always from backend — never hardcoded) ───────────────
    val planRaw   = (profile?.plan?.ifBlank { "FREE" } ?: "FREE").uppercase()
    val planLabel = when (planRaw) { "GO_PRO" -> "GO PRO"; "GO_ULTRA" -> "GO ULTRA"; else -> "FREE" }
    val isPro     = planRaw == "GO_PRO"
    val isUltra   = planRaw == "GO_ULTRA"

    // Feature-flag-based access — no more raw plan string comparisons in UI
    val hasCyberCard = hasFeature(user, Feature.CYBER_CARD)
    val hasFamilyProtection = hasFeature(user, Feature.FAMILY_PROTECTION)

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadProfilePhoto(context, it) }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        cyberCardViewModel.loadCard()
        contentVisible = true
    }
    LaunchedEffect(profile) {
        profile?.let { formState = formState.copy(name = it.name, phone = it.phone.orEmpty()) }
    }

    // Full-screen loader only on first load when there is no cached data
    if (loading && profile == null) {
        Box(
            Modifier.fillMaxSize().background(if (isDark) PC.DarkBg else PC.LightBg),
            contentAlignment = Alignment.Center
        ) { EdgyLoadingAnimation() }
        return
    }

    val localizedMessage         = message?.let { localizedUiMessage(it) }
    val localizedSecurityMessage = securityMessage?.let { localizedUiMessage(it) }

    val activeCard = if (hasCyberCard && cyberCard?.card_status?.uppercase() == "ACTIVE") {
        CyberCardUiState(
            userName    = profile?.name?.ifBlank { user?.name } ?: stringResource(R.string.profile_guest),
            cardNumber  = cyberCard?.card_id ?: formatCardNumber(user?.id ?: ""),
            cyberScore  = cyberCard?.score ?: 0,
            generatedOn = cyberCard?.score_month ?: "--",
            validTill   = cyberCard?.score_version ?: "--",
            level       = cyberCard?.level        // V2 machine level key
        )
    } else null

    // Security score — prefer cyber card score, then fall back to 0
    val securityScore = cyberCard?.score ?: 0

    // ─────────────────────────────────────────────────────────────────────────
    // ROOT LAYOUT
    // ─────────────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) PC.DarkBg else PC.LightBg)
            .verticalScroll(rememberScrollState())
    ) {

        // ── STEP 2: PREMIUM HERO CARD ─────────────────────────────────────────
        PremiumProfileHero(
            isDark          = isDark,
            name            = profile?.name?.ifBlank { user?.name } ?: stringResource(R.string.profile_guest),
            email           = profile?.email ?: user?.email ?: "",
            phone           = profile?.phone ?: user?.phone ?: "",
            imageUri        = imageUri,
            sessionImageUrl = user?.profileImageUrl,
            planRaw         = planRaw,
            planLabel       = planLabel,
            onEditPhoto     = { imagePicker.launch("image/*") }
        )

        // ── ANIMATED CONTENT ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = contentVisible,
            enter   = fadeIn(tween(480, delayMillis = 60)) +
                      slideInVertically(tween(480, delayMillis = 60)) { it / 10 }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── STEP 3: SECURITY STATUS ────────────────────────────────────
                SecurityStatusCard(isDark = isDark, riskScore = securityScore, scanCount = quota?.scan_used ?: 0, threatCount = 0)

                // ── STEP 3b: SCAN USAGE CARD (quota) ──────────────────────────
                if (quota != null) {
                    ScanUsageCard(isDark = isDark, quota = quota!!, isPro = isPro || isUltra)
                }

                // ── ULTRA FAMILY PROTECTION CARD ──────────────────────────────
                if (hasFamilyProtection) {
                    UltraFamilyCard(isDark = isDark)
                }

                // ── STEP 4: UPGRADE CARD ──────────────────────────────────────
                if (!isUltra) {
                    ProfileUpgradeCard(
                        isDark    = isDark,
                        isPro     = isPro,
                        quota     = quota,
                        onUpgrade = { viewModel.upgradeToPremium() }
                    )
                }

                // ── CYBER CARD (feature-gated, not plan-string-gated) ─────────
                CyberCardSection(
                    isPremium  = hasCyberCard,
                    activeCard = activeCard,
                    onUpgrade  = { viewModel.upgradeToPremium() }
                )

                // ── STEP 5: QUICK ACTIONS ─────────────────────────────────────
                ProfileQuickActionsSection(isDark = isDark)

                // ── STEP 6: ACCOUNT SETTINGS ──────────────────────────────────
                ProfileSectionLabel(label = "Account Settings", isDark = isDark)
                ProfileFormSection(
                    isDark         = isDark,
                    name           = formState.name,
                    phone          = formState.phone,
                    imageUri       = imageUri,
                    remoteImageUrl = profile?.profile_image_url ?: user?.profileImageUrl,
                    message        = localizedMessage,
                    onNameChange   = { formState = formState.copy(name = it) },
                    onPhoneChange  = { formState = formState.copy(phone = it) },
                    onSave         = {
                        val p = profile ?: return@ProfileFormSection
                        if (p.id.isBlank()) return@ProfileFormSection
                        viewModel.updateProfile(formState.name)
                    },
                    onEditPhoto    = { imagePicker.launch("image/*") }
                )

                // ── STEP 6 cont: SECURITY & PREFERENCES ───────────────────────
                ProfileSectionLabel(label = "Security & Preferences", isDark = isDark)
                SecuritySection(
                    isDark             = isDark,
                    securityExpanded   = dialogs.securityExpanded,
                    currentPass        = formState.currentPass,
                    newPass            = formState.newPass,
                    confirmPass        = formState.confirmPass,
                    showCurrentPass    = formState.showCurrentPass,
                    showNewPass        = formState.showNewPass,
                    showConfirmPass    = formState.showConfirmPass,
                    securityMessage    = localizedSecurityMessage,
                    onSecurityToggle   = { dialogs = dialogs.copy(securityExpanded = !dialogs.securityExpanded) },
                    onCurrentPassChange  = { formState = formState.copy(currentPass = it) },
                    onNewPassChange      = { formState = formState.copy(newPass = it) },
                    onConfirmPassChange  = { formState = formState.copy(confirmPass = it) },
                    onToggleCurrentPass  = { formState = formState.copy(showCurrentPass = !formState.showCurrentPass) },
                    onToggleNewPass      = { formState = formState.copy(showNewPass = !formState.showNewPass) },
                    onToggleConfirmPass  = { formState = formState.copy(showConfirmPass = !formState.showConfirmPass) },
                    onUpdatePassword     = {
                        securityViewModel.changePassword(formState.currentPass, formState.newPass, formState.confirmPass)
                    },
                    onShowLanguage   = { dialogs = dialogs.copy(showLanguageDialog = true) },
                    onLogoutClick    = { dialogs = dialogs.copy(showLogoutDialog = true) },
                    onDeleteClick    = {
                        if (!isDeleting) {
                            formState = formState.copy(deleteConfirmationInput = "")
                            dialogs   = dialogs.copy(showDeleteDialog = true)
                        }
                    }
                )

                // ── TRUST SIGNAL STRIP ─────────────────────────────────────
                ProfileTrustStrip(isDark = isDark)
            }
        }
    }

    // ── DIALOGS (existing, completely unchanged) ──────────────────────────────
    ProfileDialogs(
        dialogState                   = dialogs,
        deleteConfirmationInput       = deleteInput,
        currentUserName               = currentUserName,
        isDeleting                    = isDeleting,
        onDeleteConfirmationInputChange = { formState = formState.copy(deleteConfirmationInput = it) },
        onDismissLogout               = { dialogs = dialogs.copy(showLogoutDialog = false) },
        onDismissDelete               = {
            if (!isDeleting) {
                dialogs   = dialogs.copy(showDeleteDialog = false)
                formState = formState.copy(deleteConfirmationInput = "")
            }
        },
        onDismissLanguage = { dialogs = dialogs.copy(showLanguageDialog = false) },
        onConfirmLogout   = { securityViewModel.logoutAll(); onLogout() },
        onConfirmDelete   = {
            if (!isDeleteValid || isDeleting) {
                if (!isDeleting) Toast.makeText(
                    context,
                    context.getString(R.string.profile_delete_name_mismatch),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                viewModel.deleteAccount(
                    username  = deleteInput.trim(),
                    onSuccess = {
                        dialogs   = dialogs.copy(showDeleteDialog = false)
                        formState = formState.copy(deleteConfirmationInput = "")
                        onLogout()
                        activity?.finishAffinity()
                    },
                    onError   = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                )
            }
        }
    )
}

// =============================================================================
// STEP 2 — PREMIUM HERO CARD
// Clean gradient + greeting "Hi {name} 👋" + plan ring + verified badge
// NO glow / blur effects — clean professional avatar container
// =============================================================================

@Composable
private fun PremiumProfileHero(
    isDark: Boolean,
    name: String,
    email: String,
    phone: String,
    imageUri: String?,
    sessionImageUrl: String?,
    planRaw: String,
    planLabel: String,
    onEditPhoto: () -> Unit
) {
    val context    = LocalContext.current
    val avatarModel = imageUri ?: sessionImageUrl
    val firstName  = name.trim().split(" ").firstOrNull()?.ifBlank { name } ?: name
    val initials   = name.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
    val isPaid = planRaw == "GO_PRO" || planRaw == "GO_ULTRA"

    // Gradient — neutral dark tones, no green tint
    val heroGradient = when (planRaw) {
        "GO_ULTRA" -> Brush.linearGradient(listOf(Color(0xFF12022B), Color(0xFF2D1B69), Color(0xFF120228)))
        "GO_PRO"   -> Brush.linearGradient(listOf(Color(0xFF071528), Color(0xFF0F2852), Color(0xFF071330)))
        else       -> Brush.linearGradient(listOf(PC.HeroStart, PC.HeroMid, PC.HeroEnd))
    }

    // Avatar ring — subtle gradient ring, no glow
    val ringColor = when (planRaw) {
        "GO_ULTRA" -> Color(0xFFAB47BC)
        "GO_PRO"   -> Color(0xFF42A5F5)
        else       -> Color(0xFF6B7280)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(heroGradient)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        // Decorative background circles
        Box(modifier = Modifier.size(200.dp).align(Alignment.TopEnd).offset(x = 60.dp, y = (-40).dp)
            .background(Color.White.copy(alpha = 0.025f), CircleShape))
        Box(modifier = Modifier.size(120.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 30.dp)
            .background(Color.White.copy(alpha = 0.025f), CircleShape))

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            // ── Avatar — clean circle, no glow/blur ──────────────────────────
            Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f), CircleShape)
                        .border(2.dp, ringColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarModel.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarModel)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile photo",
                            modifier     = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = initials, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                // Verified badge (blue tick) — ONLY for PRO / ULTRA, never for FREE
                if (isPaid) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1D9BF0))
                            .border(1.5.dp, if (isDark) Color(0xFF0B0F14) else Color.White, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    // Edit button for free users (paid users get verified badge here)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C2B3A))
                            .border(1.5.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                            .clickable(onClick = onEditPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Edit, "Edit photo", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                    }
                }
            }

            // ── Greeting + name / contact / plan badge ────────────────────────
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // "Hi Arjun 👋" greeting
                Text(
                    text          = "Hi $firstName 👋",
                    fontSize      = 12.sp,
                    color         = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 0.2.sp
                )
                Text(
                    text          = name,
                    fontSize      = 20.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = Color.White,
                    letterSpacing = (-0.3).sp,
                    maxLines      = 1,
                    overflow      = TextOverflow.Ellipsis
                )
                val contactLine = email.ifBlank { phone }
                if (contactLine.isNotBlank()) {
                    Text(
                        text     = contactLine,
                        fontSize = 12.sp,
                        color    = Color.White.copy(alpha = 0.50f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                PlanBadge(planRaw = planRaw, planLabel = planLabel)
            }
        }
    }
}

// ── Plan badge ────────────────────────────────────────────────────────────────
@Composable
private fun PlanBadge(planRaw: String, planLabel: String) {
    val bgBrush: Brush
    val borderColor: Color
    val textColor: Color
    val prefix: String

    when (planRaw) {
        "GO_ULTRA" -> {
            bgBrush     = Brush.horizontalGradient(listOf(Color(0xFF7B1FA2), Color(0xFFAB47BC)))
            borderColor = Color(0xFFCE93D8)
            textColor   = Color.White
            prefix      = "✦  "
        }
        "GO_PRO" -> {
            bgBrush     = Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF1976D2)))
            borderColor = Color(0xFF90CAF9)
            textColor   = Color.White
            prefix      = "★  "
        }
        else -> {
            bgBrush     = Brush.horizontalGradient(listOf(Color.White.copy(0.09f), Color.White.copy(0.12f)))
            borderColor = Color.White.copy(0.20f)
            textColor   = Color.White.copy(0.65f)
            prefix      = "◆  "
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(bgBrush)
            .border(0.5.dp, borderColor, RoundedCornerShape(7.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = "$prefix$planLabel", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor, letterSpacing = 0.4.sp)
    }
}

// =============================================================================
// STEP 3 — SECURITY STATUS CARD
// Real score from cyber card, label by range, animated spring progress bar
// Score labels: 0-30 High Risk | 31-60 Needs Attention | 61-80 Mostly Safe | 81-100 Fully Protected
// =============================================================================

@Composable
private fun SecurityStatusCard(
    isDark: Boolean,
    riskScore: Int,
    scanCount: Int,
    threatCount: Int
) {
    val cardBg = if (isDark) Color(0xFF121923) else Color.White

    // Step 3 requirement: proper label mapping
    val (statusText, accentColor) = when {
        riskScore <= 30 -> "High Risk"          to Color(0xFFEF5350)
        riskScore <= 60 -> "Needs Attention"    to Color(0xFFFFA726)
        riskScore <= 80 -> "Mostly Safe"        to Color(0xFF42A5F5)
        else            -> "Fully Protected"    to (if (isDark) Color(0xFF66BB6A) else Color(0xFF2E7D32))
    }

    val progressFraction by animateFloatAsState(
        targetValue   = riskScore / 100f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "riskProgress"
    )

    Surface(
        modifier       = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(0.05f)),
        shape          = RoundedCornerShape(18.dp),
        color          = cardBg,
        tonalElevation = if (isDark) 2.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {

            // Header row
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(accentColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Shield, null, tint = accentColor, modifier = Modifier.size(18.dp))
                    }
                    Text("Your Safety Status", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1A1A1A))
                }
                Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
            }

            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$riskScore", fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = accentColor, lineHeight = 44.sp)
                    Text("/ 100", fontSize = 12.sp, color = if (isDark) Color(0xFF808080) else Color(0xFF999999))
                }
                Column(modifier = Modifier.weight(1f).padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(statusText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = accentColor, lineHeight = 18.sp)
                    // Animated progress bar (manual Box — avoids LinearProgressIndicator API version differences)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isDark) Color(0xFF2A2A2A) else Color(0xFFF0F0F0))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(progressFraction).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(accentColor))
                    }
                    Text("Last updated: Today", fontSize = 11.sp, color = if (isDark) Color(0xFF666666) else Color(0xFFAAAAAA))
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF202020) else Color(0xFFF0F0F0), thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                SecurityStatCell(value = "$scanCount", label = "Scans Done", color = if (isDark) Color(0xFF66BB6A) else Color(0xFF2E7D32))
                Box(Modifier.width(0.5.dp).height(34.dp).background(if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE)))
                SecurityStatCell(value = "$threatCount", label = "Threats Blocked", color = Color(0xFFFFA726))
                Box(Modifier.width(0.5.dp).height(34.dp).background(if (isDark) Color(0xFF2A2A2A) else Color(0xFFEEEEEE)))
                SecurityStatCell(value = "Today", label = "Last Scan", color = if (isDark) Color(0xFFB3B3B3) else Color(0xFF666666))
            }
        }
    }
}

@Composable
private fun SecurityStatCell(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = color.copy(alpha = 0.70f))
    }
}

// =============================================================================
// TRUST SIGNAL STRIP — builds confidence at the bottom of the profile
// =============================================================================

@Composable
private fun ProfileTrustStrip(isDark: Boolean) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.fillMaxWidth()
        ) {
            Text(
                text     = "🔒  Your data stays private · We never store your scans",
                fontSize = 10.sp,
                color    = if (isDark) Color(0xFF4B5563) else Color(0xFF9CA3AF),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        Text(
            text      = "GO Suraksha v2.0 · Trusted cyber safety for India",
            fontSize  = 9.sp,
            color     = if (isDark) Color(0xFF374151) else Color(0xFFD1D5DB),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// =============================================================================
// STEP 3b — SCAN USAGE CARD
// Shows quota per scan type pulled from /user/quota
// =============================================================================

@Composable
private fun ScanUsageCard(isDark: Boolean, quota: QuotaResponse, isPro: Boolean) {
    val cardBg = if (isDark) Color(0xFF121923) else Color.White
    val unlimitedLabel = if (isPro) "Unlimited" else null

    Surface(
        modifier       = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(0.04f)),
        shape          = RoundedCornerShape(18.dp),
        color          = cardBg,
        tonalElevation = if (isDark) 2.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF42A5F5).copy(0.12f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Security, null, tint = Color(0xFF42A5F5), modifier = Modifier.size(16.dp))
                }
                Text("Your Protection Usage", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1A1A1A))
                if (isPro) {
                    Spacer(Modifier.weight(1f))
                    Text("PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF42A5F5),
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF42A5F5).copy(0.12f)).padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            HorizontalDivider(color = if (isDark) Color(0xFF1E2530) else Color(0xFFF0F4F8), thickness = 0.5.dp)
            QuotaRow("Photo & Image Checks", quota.image_scans_used, quota.image_scan_limit, unlimitedLabel, isDark)
            QuotaRow("Message & Link Scans", quota.text_scans_used,  quota.text_scan_limit,  unlimitedLabel, isDark)
            QuotaRow("QR Code Scans",        quota.qr_scans_used,    quota.qr_scan_limit,    unlimitedLabel, isDark)
        }
    }
}

@Composable
private fun QuotaRow(label: String, used: Int, limit: Int, unlimitedLabel: String?, isDark: Boolean) {
    val fraction = if (limit > 0) (used.toFloat() / limit).coerceIn(0f, 1f) else 0f
    val barColor = when {
        unlimitedLabel != null -> Color(0xFF42A5F5)
        fraction >= 0.9f       -> Color(0xFFEF5350)
        fraction >= 0.6f       -> Color(0xFFFFA726)
        else                   -> Color(0xFF66BB6A)
    }
    val progressAnim by animateFloatAsState(
        targetValue   = if (unlimitedLabel != null) 1f else fraction,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "quotaProgress$label"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF555555))
            Text(
                text      = if (unlimitedLabel != null) "∞ Unlimited" else "$used / $limit",
                fontSize  = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color     = barColor
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(if (isDark) Color(0xFF2A2A2A) else Color(0xFFF0F0F0))) {
            Box(modifier = Modifier.fillMaxWidth(progressAnim).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(barColor))
        }
    }
}

// =============================================================================
// ULTRA FAMILY PROTECTION CARD
// Shown only for GO_ULTRA users with Feature.FAMILY_PROTECTION
// =============================================================================

@Composable
private fun UltraFamilyCard(isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1A0535), Color(0xFF3D1B6E), Color(0xFF1A0535))))
            .padding(18.dp)
    ) {
        // Decorative circle
        Box(modifier = Modifier.size(100.dp).align(Alignment.TopEnd).offset(x = 30.dp, y = (-20).dp)
            .background(Color.White.copy(0.04f), CircleShape))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFCE93D8).copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.FamilyRestroom, null, tint = Color(0xFFCE93D8), modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Family Protection Active", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Box(modifier = Modifier.size(7.dp).background(Color(0xFF66BB6A), CircleShape))
                }
                Text("Protected by GO ULTRA", fontSize = 11.sp, color = Color(0xFFCE93D8).copy(0.80f))
            }
        }
    }
}

// =============================================================================
// STEP 4 — UPGRADE CARD
// Contextual messaging:
//   FREE → "You've reached your scan limit"
//   PRO  → "Unlock real-time alerts & family protection"
// =============================================================================

@Composable
private fun ProfileUpgradeCard(
    isDark: Boolean,
    isPro: Boolean,
    quota: QuotaResponse?,
    onUpgrade: () -> Unit
) {
    val gradientColors = if (isPro)
        listOf(Color(0xFF12022B), Color(0xFF2D1B69), Color(0xFF12022B))
    else
        listOf(Color(0xFF071528), Color(0xFF0F2852), Color(0xFF071528))

    val badgeText = if (isPro) "✦  GO ULTRA" else "★  GO PRO"
    val badgeColor = if (isPro) Color(0xFFCE93D8) else Color(0xFF90CAF9)

    // Contextual headline — outcome-based, not feature-based
    val headline = if (isPro)
        "Protect your entire family in real-time"
    else {
        val nearLimit = quota != null && quota.scan_used >= (quota.scan_limit * 0.85f).toInt()
        if (nearLimit) "Running low — don't leave gaps in your protection" else "Are you fully protected?"
    }
    val subtitle = if (isPro)
        "Most scams target families. GO ULTRA monitors everyone."
    else
        "Most scams go undetected. GO PRO catches them all."

    val ctaLabel     = if (isPro) "Protect My Family — GO ULTRA" else "Stay Safe Every Day — GO PRO"
    val ctaTextColor = if (isPro) Color(0xFF2D1B69) else Color(0xFF0F2852)

    val benefits = if (isPro)
        listOf("See if your family receives risky messages", "Unlimited scans & AI reports", "Real-time family threat alerts")
    else
        listOf("Scan as many messages as you need", "AI explains exactly why something is risky", "Get warned before you get scammed")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable(onClick = onUpgrade)
            .padding(18.dp)
    ) {
        Box(modifier = Modifier.size(130.dp).align(Alignment.TopEnd).offset(x = 45.dp, y = (-35).dp)
            .background(Color.White.copy(0.035f), CircleShape))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor, letterSpacing = 0.8.sp)
                Text(headline,  fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 21.sp)
                Text(subtitle,  fontSize = 12.sp, color = Color.White.copy(0.60f))
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                benefits.forEach { benefit ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(17.dp).background(Color.White.copy(0.10f), CircleShape), contentAlignment = Alignment.Center) {
                            Text("✓", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(benefit, fontSize = 12.sp, color = Color.White.copy(0.85f))
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Button(
                onClick        = onUpgrade,
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(11.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ctaTextColor),
                contentPadding = PaddingValues(vertical = 13.dp)
            ) {
                Text(ctaLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =============================================================================
// STEP 5 — QUICK ACTIONS GRID
// 2×2 grid — Alerts tile shows badge if > 0 alerts (feature-extensible)
// =============================================================================

@Composable
private fun ProfileQuickActionsSection(isDark: Boolean, alertBadgeCount: Int = 0) {
    val cardBg = if (isDark) Color(0xFF121923) else Color.White

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileSectionLabel(label = "Quick Actions", isDark = isDark)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionTile(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Outlined.People,
                title     = "Trusted Contacts",
                subtitle  = "Emergency contacts",
                iconColor = Color(0xFF42A5F5),
                iconBg    = Color(0xFF42A5F5).copy(0.12f),
                cardBg    = cardBg,
                isDark    = isDark,
                badge     = null,
                onClick   = {}
            )
            QuickActionTile(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Outlined.Notifications,
                title     = "Alerts",
                subtitle  = "View recent alerts",
                iconColor = Color(0xFFFFA726),
                iconBg    = Color(0xFFFFA726).copy(0.12f),
                cardBg    = cardBg,
                isDark    = isDark,
                badge     = if (alertBadgeCount > 0) alertBadgeCount else null,
                onClick   = {}
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionTile(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Outlined.AccessTime,
                title     = "Scan History",
                subtitle  = "Past scan results",
                iconColor = Color(0xFF66BB6A),
                iconBg    = Color(0xFF66BB6A).copy(0.12f),
                cardBg    = cardBg,
                isDark    = isDark,
                badge     = null,
                onClick   = {}
            )
            QuickActionTile(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Outlined.Lock,
                title     = "Privacy",
                subtitle  = "Settings & controls",
                iconColor = Color(0xFFAB47BC),
                iconBg    = Color(0xFFAB47BC).copy(0.12f),
                cardBg    = cardBg,
                isDark    = isDark,
                badge     = null,
                onClick   = {}
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    iconBg: Color,
    cardBg: Color,
    isDark: Boolean,
    badge: Int?,
    onClick: () -> Unit
) {
    Surface(
        modifier       = modifier
            .shadow(2.dp, RoundedCornerShape(15.dp), ambientColor = Color.Black.copy(0.04f))
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        shape          = RoundedCornerShape(15.dp),
        color          = cardBg,
        tonalElevation = if (isDark) 2.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Box {
                Box(modifier = Modifier.size(38.dp).background(iconBg, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                // Badge dot
                if (badge != null) {
                    Box(
                        modifier = Modifier.size(16.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)
                            .background(Color(0xFFEF5350), CircleShape)
                            .border(1.5.dp, cardBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (badge > 99) "99+" else "$badge", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title,    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1A1A1A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, fontSize = 11.sp, color = if (isDark) Color(0xFFB3B3B3) else Color(0xFF888888), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// =============================================================================
// UTILITY — section label (uppercase, muted, letter-spaced)
// =============================================================================

@Composable
private fun ProfileSectionLabel(label: String, isDark: Boolean) {
    Text(
        text          = label.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        color         = if (isDark) Color(0xFF666666) else Color(0xFFAAAAAA),
        letterSpacing = 0.7.sp,
        modifier      = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
    )
}
