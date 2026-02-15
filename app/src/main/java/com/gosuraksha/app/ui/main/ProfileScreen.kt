package com.gosuraksha.app.ui.main

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.data.SessionManager
import com.gosuraksha.app.profile.ProfileViewModel
import com.gosuraksha.app.security.SecurityViewModel
import com.gosuraksha.app.ui.components.*

// ── Palette ───────────────────────────────────────────────────────────────────
private val ProfileBg     = Color(0xFF0D1117)
private val CardDark      = Color(0xFF161B22)
private val CardBorder    = Color(0xFF30363D)
private val CyberTeal     = Color(0xFF00E5C3)
private val DangerRed     = Color(0xFFFF3B5C)
private val WarnAmber     = Color(0xFFFFB020)
private val SafeGreen     = Color(0xFF00D68F)
private val PurpleAccent  = Color(0xFF7C3AED)
private val TextPrimary   = Color(0xFFE6EDF3)
private val TextSecondary = Color(0xFF8B949E)

// =============================================================================
// ProfileScreen
// =============================================================================
@Composable
fun ProfileScreen(onLogout: () -> Unit) {

    val viewModel: ProfileViewModel         = viewModel()
    val securityViewModel: SecurityViewModel = viewModel()
    val cyberCardViewModel: CyberCardViewModel = viewModel()

    val profile        by viewModel.profile.collectAsState()
    val loading        by viewModel.loading.collectAsState()
    val message        by viewModel.message.collectAsState()
    val securityMessage by securityViewModel.message.collectAsState()
    val user           by SessionManager.user.collectAsState()
    val cyberCard      by cyberCardViewModel.card.collectAsState()

    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showLogoutDialog  by remember { mutableStateOf(false) }
    var name         by remember { mutableStateOf("") }
    var phone        by remember { mutableStateOf("") }
    var currentPass  by remember { mutableStateOf("") }
    var newPass      by remember { mutableStateOf("") }
    var confirmPass  by remember { mutableStateOf("") }
    var showCurrentPass by remember { mutableStateOf(false) }
    var showNewPass     by remember { mutableStateOf(false) }
    var showConfirmPass by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<String?>("profile") }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        cyberCardViewModel.loadCard()
    }
    LaunchedEffect(profile) {
        profile?.let { name = it.name; phone = it.phone ?: "" }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyberTeal)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBg)
            .verticalScroll(rememberScrollState())
    ) {

        // ── IDENTITY HERO ─────────────────────────────────────────────
        IdentityHero(userName = user?.name, userEmail = user?.email)

        Spacer(Modifier.height(20.dp))

        // ── CYBER CARD ────────────────────────────────────────────────
        user?.let { currentUser ->
            when (cyberCard?.card_status) {
                "ACTIVE" -> CyberCardContainer(
                    user        = currentUser.copy(id = cyberCard!!.card_id!!),
                    cyberScore  = cyberCard!!.score!!,
                    maxScore    = cyberCard!!.max_score!!,
                    riskLevel   = cyberCard!!.risk_level!!,
                    generatedOn = cyberCard!!.score_month!!,
                    validTill   = calculateValidTill(cyberCard!!.score_month!!),
                    signals     = cyberCard!!.signals
                )
                "LOCKED"  -> LockedMonthCard(score = cyberCard!!.score ?: 600, message = cyberCard!!.message ?: "Locked this month")
                "PENDING" -> PendingCard(message = cyberCard!!.message ?: "Card will be available next month")
                else      -> LockedCyberCard { showUpgradeDialog = true }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── SECURITY SCORE RING ───────────────────────────────────────
        SecurityScoreRing(modifier = Modifier.padding(horizontal = 20.dp))

        Spacer(Modifier.height(20.dp))

        // ── QUICK STATS ───────────────────────────────────────────────
        QuickStatRow(modifier = Modifier.padding(horizontal = 20.dp))

        Spacer(Modifier.height(24.dp))

        // ── PROFILE SECTION ───────────────────────────────────────────
        AccordionSection(
            title    = "Profile Information",
            icon     = Icons.Outlined.Person,
            accent   = CyberTeal,
            expanded = expandedSection == "profile",
            onToggle = { expandedSection = if (expandedSection == "profile") null else "profile" },
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Outlined.Person
                )
                ProfileField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    icon = Icons.Outlined.Phone
                )
                Spacer(Modifier.height(4.dp))
                GradientButton(
                    text    = "Save Changes",
                    colors  = listOf(CyberTeal, Color(0xFF00B89C)),
                    onClick = { viewModel.updateProfile(name, phone) }
                )
                message?.let {
                    FeedbackBanner(text = it, color = SafeGreen)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── SECURITY SECTION ──────────────────────────────────────────
        AccordionSection(
            title    = "Security & Password",
            icon     = Icons.Outlined.Lock,
            accent   = WarnAmber,
            expanded = expandedSection == "security",
            onToggle = { expandedSection = if (expandedSection == "security") null else "security" },
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileField(
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    label = "Current Password",
                    icon = Icons.Outlined.Lock,
                    isPassword = true,
                    showPassword = showCurrentPass,
                    onTogglePassword = { showCurrentPass = !showCurrentPass }
                )
                ProfileField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = "New Password",
                    icon = Icons.Outlined.LockOpen,
                    isPassword = true,
                    showPassword = showNewPass,
                    onTogglePassword = { showNewPass = !showNewPass }
                )
                ProfileField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = "Confirm Password",
                    icon = Icons.Outlined.LockOpen,
                    isPassword = true,
                    showPassword = showConfirmPass,
                    onTogglePassword = { showConfirmPass = !showConfirmPass }
                )

                // Password strength indicator
                if (newPass.isNotEmpty()) {
                    PasswordStrengthBar(password = newPass)
                }

                Spacer(Modifier.height(4.dp))
                GradientButton(
                    text   = "Update Password",
                    colors = listOf(WarnAmber, Color(0xFFE09000)),
                    onClick = {
                        securityViewModel.changePassword(currentPass, newPass, confirmPass)
                    }
                )
                securityMessage?.let {
                    FeedbackBanner(text = it, color = SafeGreen)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── DANGER ZONE ───────────────────────────────────────────────
        DangerZone(
            onLogoutAll = { showLogoutDialog = true },
            modifier    = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(100.dp))
    }

    // ── UPGRADE DIALOG ────────────────────────────────────────────────
    if (showUpgradeDialog) {
        CyberDialog(
            title   = "Upgrade Required",
            message = "Upgrade to Premium to access your Cyber Card and advanced threat intelligence.",
            confirm = "Upgrade Now",
            dismiss = "Not Now",
            onConfirm = { showUpgradeDialog = false },
            onDismiss = { showUpgradeDialog = false }
        )
    }

    // ── LOGOUT CONFIRM DIALOG ─────────────────────────────────────────
    if (showLogoutDialog) {
        CyberDialog(
            title    = "Logout All Sessions?",
            message  = "This will sign you out from all devices immediately.",
            confirm  = "Logout All",
            dismiss  = "Cancel",
            isDanger = true,
            onConfirm = {
                securityViewModel.logoutAll()
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

// =============================================================================
// IdentityHero
// =============================================================================
@Composable
private fun IdentityHero(userName: String?, userEmail: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Gradient bg
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0D1F2D), ProfileBg)
                    )
                )
        )
        // Animated rotating accent
        val infinite = rememberInfiniteTransition(label = "hero_rot")
        val rot by infinite.animateFloat(
            initialValue  = 0f,
            targetValue   = 360f,
            animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
            label         = "rot"
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .rotate(rot)
                .background(Color.Transparent)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = CyberTeal.copy(alpha = 0.06f),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f)
                    )
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(CyberTeal.copy(alpha = 0.2f), Color(0xFF161B22))
                        )
                    )
                    .border(2.dp, CyberTeal.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initials = userName
                    ?.split(" ")
                    ?.mapNotNull { it.firstOrNull()?.toString() }
                    ?.take(2)?.joinToString("") ?: "?"
                Text(
                    text = initials,
                    color = CyberTeal,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = userName ?: "User",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (!userEmail.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(text = userEmail, color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            // Verified badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(CyberTeal.copy(alpha = 0.1f))
                    .border(1.dp, CyberTeal.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.VerifiedUser, null, tint = CyberTeal, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text("Verified Account", color = CyberTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// =============================================================================
// SecurityScoreRing
// =============================================================================
@Composable
private fun SecurityScoreRing(modifier: Modifier = Modifier) {
    // Hardcoded score — wire to your ViewModel when ready
    val score     = 72
    val progress  = score / 100f
    val scoreColor = when {
        score >= 75 -> SafeGreen
        score >= 50 -> WarnAmber
        else        -> DangerRed
    }
    val animProg by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "score_ring"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ring
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = CardBorder, style = Stroke(width = 10f))
                drawArc(
                    color      = scoreColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animProg,
                    useCenter  = false,
                    style      = Stroke(width = 10f, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$score", color = scoreColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("/100", color = TextSecondary, fontSize = 9.sp)
            }
        }

        Spacer(Modifier.width(20.dp))

        Column(Modifier.weight(1f)) {
            Text("SECURITY SCORE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = when {
                    score >= 75 -> "Good Standing"
                    score >= 50 -> "Needs Attention"
                    else        -> "At Risk"
                },
                color = scoreColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text("Based on your scan history and threat detections.", color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

// =============================================================================
// QuickStatRow
// =============================================================================
@Composable
private fun QuickStatRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(label = "Scans",    value = "34",  color = CyberTeal,   modifier = Modifier.weight(1f))
        StatChip(label = "Threats",  value = "22",  color = DangerRed,   modifier = Modifier.weight(1f))
        StatChip(label = "Resolved", value = "18",  color = SafeGreen,   modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

// =============================================================================
// AccordionSection
// =============================================================================
@Composable
private fun AccordionSection(
    title: String,
    icon: ImageVector,
    accent: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.dp, if (expanded) accent.copy(alpha = 0.3f) else CardBorder, RoundedCornerShape(20.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Content
        if (expanded) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
            Column(modifier = Modifier.padding(18.dp)) {
                content()
            }
        }
    }
}

// =============================================================================
// ProfileField
// =============================================================================
@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(icon, null, tint = CyberTeal, modifier = Modifier.size(18.dp)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = CyberTeal,
            unfocusedBorderColor = CardBorder,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = CyberTeal,
            focusedLabelColor    = CyberTeal,
            unfocusedLabelColor  = TextSecondary,
            focusedContainerColor   = ProfileBg,
            unfocusedContainerColor = ProfileBg
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

// =============================================================================
// PasswordStrengthBar
// =============================================================================
@Composable
private fun PasswordStrengthBar(password: String) {
    val strength = when {
        password.length >= 12 && password.any { it.isUpperCase() } && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 3
        password.length >= 8 -> 2
        else -> 1
    }
    val label = listOf("", "Weak", "Fair", "Strong", "Very Strong")[strength]
    val color = listOf(Color.Transparent, DangerRed, WarnAmber, SafeGreen, CyberTeal)[strength]

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i < strength) color else CardBorder)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// =============================================================================
// GradientButton
// =============================================================================
@Composable
private fun GradientButton(
    text: String,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(colors), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = ProfileBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// =============================================================================
// FeedbackBanner
// =============================================================================
@Composable
private fun FeedbackBanner(text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = color, fontSize = 13.sp)
    }
}

// =============================================================================
// DangerZone
// =============================================================================
@Composable
private fun DangerZone(onLogoutAll: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.dp, DangerRed.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Warning, null, tint = DangerRed, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("DANGER ZONE", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(DangerRed.copy(alpha = 0.15f)))

        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Logging out all sessions will immediately revoke access on all devices.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Button(
                onClick = onLogoutAll,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.Logout, null, tint = DangerRed, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout All Sessions", color = DangerRed, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

// =============================================================================
// CyberDialog
// =============================================================================
@Composable
private fun CyberDialog(
    title: String,
    message: String,
    confirm: String,
    dismiss: String,
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Text(message, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDanger) DangerRed else CyberTeal
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(confirm, color = if (isDanger) Color.White else ProfileBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismiss, color = TextSecondary)
            }
        }
    )
}

// =============================================================================
// calculateValidTill
// =============================================================================
private fun calculateValidTill(scoreMonth: String): String {
    return try {
        val month = java.time.LocalDate.parse(scoreMonth.substring(0, 10))
        month.plusMonths(1).toString()
    } catch (e: Exception) { scoreMonth }
}