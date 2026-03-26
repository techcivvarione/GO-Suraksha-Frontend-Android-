package com.gosuraksha.app.ui.auth

import android.app.Application
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.profile.ProfileViewModel
import com.gosuraksha.app.profile.ProfileViewModelFactory
import com.gosuraksha.app.ui.components.FullScreenLoader

// ── Hero colours (always dark — not theme-adaptive) ───────────────────────────
private val HeroBg      = Color(0xFF060E08)
private val AccentGreen = Color(0xFF52B788)
private val AccentDark  = Color(0xFF2D6A4F)
private val StepDone    = Color(0xFF2D6A4F)
private val StepActive  = Color(0xFF2D6A4F)

@Composable
fun ProfileSetupScreen(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val app     = context.applicationContext as Application
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(app))

    val loading by profileViewModel.loading.collectAsStateWithLifecycle()
    val isDark  = ColorTokens.LocalAppDarkMode.current

    var name           by remember { mutableStateOf("") }
    var imageUri       by remember { mutableStateOf<Uri?>(null) }
    var localError     by remember { mutableStateOf<String?>(null) }
    var submitRequested by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri  = uri
        localError = null
    }

    BackHandler(onBack = onBackToLogin)

    LaunchedEffect(loading) {
        if (!loading) submitRequested = false
    }

    Box(modifier = Modifier.fillMaxSize().background(HeroBg)) {

        // ── Background: card colour fills bottom half so no dark gap ──────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.BottomCenter)
                .background(AuthColors.card(isDark))
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Dark hero ─────────────────────────────────────────────────
            AuthHeroDark(
                title    = "Complete your\nprofile",
                subtitle = "One last step before you're protected",
                height   = 200.dp
            )

            // ── Scrollable card body ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AuthCardSurface(isDark = isDark) {

                    // ── Step progress pill ────────────────────────────────
                    StepProgressPill(isDark = isDark)

                    // ── Avatar picker ─────────────────────────────────────
                    AvatarPicker(
                        imageUri = imageUri,
                        isDark   = isDark,
                        onClick  = { imagePicker.launch("image/*") }
                    )

                    // ── Name field ────────────────────────────────────────
                    AuthTextField(
                        value         = name,
                        onValueChange = { name = it; localError = null },
                        label         = "Full Name",
                        placeholder   = "Enter your name",
                        leadingIcon   = Icons.Outlined.Person,
                        isDark        = isDark
                    )

                    // ── CTA ───────────────────────────────────────────────
                    AuthPrimaryButton(
                        text      = "Continue to Home",
                        onClick   = {
                            val safeName = name.trim()
                            if (safeName.isBlank()) {
                                localError = "Please enter your name to continue"
                            } else if (!submitRequested) {
                                submitRequested = true
                                profileViewModel.completeProfileSetup(
                                    context  = context,
                                    name     = safeName,
                                    imageUri = imageUri,
                                    onSuccess = {
                                        authViewModel.completeProfileSetup(
                                            onSuccess = {},
                                            onError   = { localError = it }
                                        )
                                    },
                                    onError = {
                                        submitRequested = false
                                        localError      = it
                                    }
                                )
                            }
                        },
                        enabled   = name.trim().isNotBlank() && !loading,
                        isLoading = loading,
                        isDark    = isDark
                    )

                    // ── Error ─────────────────────────────────────────────
                    localError?.let { AuthErrorRow(it, isDark) }

                    // ── Back link ─────────────────────────────────────────
                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "← Back to login",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = AuthColors.textSec(isDark),
                            modifier   = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = onBackToLogin
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        FullScreenLoader(visible = loading)
    }
}

// =============================================================================
// StepProgressPill — "Verified ✓ ──── 2 Profile"
// =============================================================================
@Composable
private fun StepProgressPill(isDark: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AuthColors.field(isDark))
                .border(
                    width = 1.dp,
                    color = AuthColors.border(isDark),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Step 1 — done
                Box(
                    modifier         = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(StepDone),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = androidx.compose.material.icons.Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(11.dp)
                    )
                }
                Text(
                    text       = "Verified",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color      = AuthColors.textSec(isDark)
                )

                // Connector
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(1.dp)
                        .background(AuthColors.border(isDark))
                )

                // Step 2 — active
                Box(
                    modifier         = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(StepActive),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "2",
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
                Text(
                    text       = "Profile",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AccentGreen
                )
            }
        }
    }
}

// =============================================================================
// AvatarPicker — large centered avatar with camera badge overlay
// =============================================================================
@Composable
private fun AvatarPicker(
    imageUri: Uri?,
    isDark:   Boolean,
    onClick:  () -> Unit
) {
    Column(
        modifier              = Modifier.fillMaxWidth(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onClick
                )
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        if (isDark) Color(0xFF1A2E22)
                        else Color(0xFFE8F4ED)
                    )
                    .border(
                        width = 2.5.dp,
                        color = AccentGreen.copy(alpha = if (imageUri != null) 1f else 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model              = imageUri,
                        contentDescription = "Profile photo",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Outlined.Person,
                        contentDescription = null,
                        tint               = AccentDark,
                        modifier           = Modifier.size(32.dp)
                    )
                }
            }

            // Camera badge — bottom right
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(AccentDark)
                    .border(2.dp, AuthColors.card(isDark), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.CameraAlt,
                    contentDescription = "Change photo",
                    tint               = Color.White,
                    modifier           = Modifier.size(13.dp)
                )
            }
        }

        Text(
            text       = if (imageUri == null) "Add profile photo · optional"
            else "Change profile photo",
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = AccentGreen,
            modifier   = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
        )
    }
}

// ── needed import for CheckCircle filled icon ─────────────────────────────────