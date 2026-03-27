package com.gosuraksha.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.domain.model.Feature
import com.gosuraksha.app.domain.model.hasFeature

// ── Surface colour palette ────────────────────────────────────────────────────

private fun screenBg(isDark: Boolean) = if (isDark) Color(0xFF060D16) else Color(0xFFF3F6F3)
fun cardBg(isDark: Boolean)  = if (isDark) Color(0xFF0D1A10) else Color(0xFFFFFFFF)
fun panelBg(isDark: Boolean) = if (isDark) Color(0xFF111E13) else Color(0xFFF0F6F1)
fun chipBg(isDark: Boolean)  = if (isDark) Color(0xFF162819) else Color(0xFFE8F5EC)
fun onSurf(isDark: Boolean)  = if (isDark) Color(0xFFD4EDD9) else Color(0xFF0D1F14)
fun subText(isDark: Boolean) = if (isDark) Color(0xFF6BAA80) else Color(0xFF5A8A6A)
fun divider(isDark: Boolean) = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000)

val Green400 = Color(0xFF2EC472)

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun CyberCardScreen(
    onBack: () -> Unit,
    onNavigateToScan: () -> Unit,
    onUpgradePlan: () -> Unit
) {
    val isDark  = ColorTokens.LocalAppDarkMode.current
    val context = LocalContext.current
    val app     = context.applicationContext as android.app.Application

    val viewModel: CyberCardViewModel = viewModel(factory = CyberCardViewModelFactory(app))

    val card               by viewModel.card.collectAsStateWithLifecycle()
    val loading            by viewModel.loading.collectAsStateWithLifecycle()
    val error              by viewModel.error.collectAsStateWithLifecycle()
    val user               by SessionManager.user.collectAsStateWithLifecycle()
    val improvementMessage by viewModel.improvementMessage.collectAsStateWithLifecycle()

    // ── Legal disclaimer (one-time) ───────────────────────────────────────────
    var disclaimerAccepted by remember {
        mutableStateOf(hasAcceptedCyberCardDisclaimer(context))
    }

    // ── Improvement toast ─────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(improvementMessage) {
        if (improvementMessage != null) {
            snackbarHostState.showSnackbar(improvementMessage!!)
            viewModel.clearImprovementMessage()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadCard() }

    Scaffold(
        containerColor = screenBg(isDark),
        topBar         = { CyberCardHeader(isDark = isDark, onBack = onBack) },
        snackbarHost   = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData   = data,
                    containerColor = Color(0xFF122212),
                    contentColor   = Green400,
                    actionColor    = Green400
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg(isDark))
                .padding(innerPadding)
        ) {
            when {
                loading -> LoadingCyberCardState()

                !hasFeature(user, Feature.CYBER_CARD) ->
                    FreeCyberCardState(isDark = isDark, onUpgradePlan = onUpgradePlan)

                card == null -> PreparingCyberCardState(
                    isDark  = isDark,
                    // pollCard = silent; no loading spinner on manual retry
                    onRetry = { viewModel.pollCard() }
                )

                else -> when (card?.card_status?.uppercase()) {
                    "PENDING" -> PendingCyberCardState(
                        isDark            = isDark,
                        onNavigateToScan  = onNavigateToScan,
                        eligible          = card?.eligible ?: false,
                        distinctScanTypes = card?.distinct_scan_types ?: 0,
                        // pollCard = silent; no loading spinner on manual retry
                        onRetry           = { viewModel.pollCard() }
                    )
                    "LOCKED"  -> LockedCyberCardState(
                        isDark        = isDark,
                        card          = card!!,
                        onUpgradePlan = onUpgradePlan
                    )
                    "ACTIVE"  -> ActiveCardScrollContent(
                        isDark           = isDark,
                        card             = card!!,
                        userName         = user?.name.orEmpty(),
                        onNavigateToScan = onNavigateToScan,
                        onRefreshCard    = { actionId -> viewModel.refreshCard(actionId) }
                    )
                    else -> ErrorCyberCardState(
                        isDark  = isDark,
                        message = error ?: "Unable to load Cyber Card",
                        onRetry = { viewModel.loadCard() }
                    )
                }
            }
        }

        // ── One-time legal disclaimer ─────────────────────────────────────────
        if (!disclaimerAccepted) {
            CyberCardDisclaimerDialog(isDark = isDark) {
                markCyberCardDisclaimerAccepted(context)
                disclaimerAccepted = true
            }
        }
    }
}

// ── Preparing state ────────────────────────────────────────────────────────────

@Composable
private fun PreparingCyberCardState(isDark: Boolean, onRetry: () -> Unit) {
    CalculatingCyberCardState(isDark = isDark, onRetry = onRetry)
}

// ── ACTIVE STATE — pop-up from bottom ─────────────────────────────────────────
//
// Entry sequence:
//   0 ms  → bg scale 1.04 → 1.0 (200 ms, EaseOut)
//   0 ms  → dim overlay alpha 0 → 0.06 (200 ms)
//   120 ms → content slides up from +500 dp with spring (MediumBouncy / Low)
//
@Composable
private fun ActiveCardScrollContent(
    isDark: Boolean,
    card: CyberCardResponse,
    userName: String,
    onNavigateToScan: () -> Unit,
    onRefreshCard: (actionId: String?) -> Unit
) {
    val insights = card.insights ?: emptyList()
    val actions  = card.actions.toCyberActions()

    // Step 1 — bg scale relaxes from 1.04 → 1.0 over 300 ms
    var bgReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { bgReady = true }

    val bgScale by animateFloatAsState(
        targetValue   = if (bgReady) 1f else 1.04f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label         = "cyber_bg_scale"
    )

    // Dim overlay fades in with bg
    val dimAlpha by animateFloatAsState(
        targetValue   = if (bgReady) 0.06f else 0f,
        animationSpec = tween(durationMillis = 200),
        label         = "cyber_dim"
    )

    // Step 2 — content slides up from bottom (delayed 120 ms)
    var contentReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120)
        contentReady = true
    }

    val contentOffset by animateDpAsState(
        targetValue   = if (contentReady) 0.dp else 480.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "cyber_slide_up"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Scaled background ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = bgScale; scaleY = bgScale }
                .background(screenBg(isDark))
        )

        // ── Dim overlay ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha))
        )

        // ── Sliding content ───────────────────────────────────────────────────
        AnimatedVisibility(
            visible = contentReady,
            enter   = fadeIn(tween(180))
        ) {
            LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .offset(y = contentOffset),
                contentPadding      = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Score card + ring panel + breakdown
                item {
                    ActiveCyberCardContent(
                        isDark   = isDark,
                        card     = card,
                        userName = userName
                    )
                }

                // Staggered insights + tappable actions
                item {
                    CyberHistorySection(
                        isDark        = isDark,
                        insights      = insights,
                        actions       = actions,
                        onActionClick = { action ->
                            when (action.id) {
                                "scan_now",
                                "scan_email",
                                "scan_password",
                                "resume_scanning" -> onNavigateToScan()
                                else              -> { /* future: deep link */ }
                            }
                            onRefreshCard(action.id)
                        }
                    )
                }

                item { DisclaimerText(isDark = isDark) }
            }
        }
    }
}

@Composable
private fun DisclaimerText(isDark: Boolean) {
    Text(
        text     = "This is GO Suraksha's internal safety score. It is not a government-issued rating.",
        color    = subText(isDark).copy(alpha = 0.50f),
        fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}
